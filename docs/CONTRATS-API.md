# CONTRATS-API.md — DSI Academy Android (Kotlin)

> Document de référence pour le codeur (Grok). **Source de vérité** : le code du site `dsiacademy--main`
> (dossier `src/services/api/*`), lu en entier le 23/10/2026. Rien n'est inventé : tout ce qui n'est pas
> confirmé par le code est marqué **[À CONFIRMER]**. En cas de doute, ne pas deviner : poser la question à l'architecte.
>
> Ce document décrit **ce que les Workers acceptent et renvoient**. Il ne décrit pas l'interface.
> Les modules `:data:*` implémentent ces contrats ; `:feature:*` ne les voit jamais (voir ARCHITECTURE.md).

---

## 0. Règles absolues

1. **Une route = une fonction** dans une interface Retrofit (`XxxApi.kt`) du module `:data:<domaine>`. Aucune route absente de ce document ne doit être appelée.
2. **Jamais** d'identifiant utilisateur (`userId`, `identifiantDsi`) dans un corps ou une URL quand le contrat dit « l'identité vient du token ». Le serveur la déduit de l'authentification.
3. **Le serveur décide**, le client affiche. Aucune règle métier recopiée (plafonds de retrait, quotas d'appel, droits d'accès, prix réels). Les valeurs de `pricing.js` ne servent qu'à l'affichage.
4. **Jamais décoder** `accessProof` ni `paymentTrackingToken` : chaînes opaques, transmises telles quelles.
5. **Jamais de cache disque** pour : session, `accessProof`, URLs temporaires de lecture (`read_url`, `playback_url`, `read-url`), `paymentTrackingToken` expiré.
6. Les messages d'erreur affichés viennent de `strings.xml` (FR/EN), indexés par **code d'erreur** (voir §1.6). On n'affiche jamais le texte brut d'une exception.
7. `Content-Type: application/json` partout, sauf envois de fichiers (`multipart/form-data`, frontière posée par OkHttp, jamais à la main).

---

## 1. Principes transverses

### 1.1 Les 6 Workers (URLs de base)

| Clé Kotlin (`WorkerUrls`) | Domaine | URL de base (défaut du site) |
|---|---|---|
| `PAYMENT` | Paiement / Membre / Auth | `https://dsi-worker-paiement.azo9-com234-az09.workers.dev` |
| `CONTENT` | Contenu (formations, vidéos, audio, bibliothèque) | `https://dsi-worker.legiss2517.workers.dev` |
| `SHOP` | Boutique | `https://dsi-worker-boutique.azo9-digitalservice.workers.dev` |
| `MARKETPLACE` | Marketplace | `https://dsi-worker-marketplace.raza-9-ck2261.workers.dev` |
| `COMMUNITY` | Communauté | `https://dsi-worker-community.dave2-kiemde2517.workers.dev` |
| `ADMIN` | Admin (une seule route publique utilisée) | `https://dsi-worker-admin.kiemdedavid23.workers.dev` |

- Les URLs doivent être **configurables par build** (`BuildConfig` / `local.properties`), jamais en dur dans les écrans.
- `API_BASE_URL` (`https://dsi-api.example.com`) est un **placeholder inutilisé** du site : ne pas le reprendre.

### 1.2 Trois mécanismes d'authentification (jamais mélangés)

| Mécanisme | En-tête | Worker(s) | Origine de la valeur |
|---|---|---|---|
| **Session membre** | `Authorization: Bearer <token>` | Paiement uniquement | `token` renvoyé par `POST /api/auth/connexion` |
| **Access Proof** | `X-Access-Proof: <accessProof>` | Contenu, Boutique, Marketplace, Communauté | `accessProof` renvoyé par `POST /api/auth/connexion` |
| **Tracking de paiement** | `X-Payment-Tracking-Token: <token>` | Paiement, route `/api/paiement/statut` uniquement | `paymentTrackingToken` (inscription ou connexion `pending_payment`) ; **expire sous 24 h** |

Règles :
- Le **Bearer n'est jamais accepté** par Boutique/Communauté/Marketplace/Contenu, et l'**Access Proof n'est jamais utilisé** sur le Worker Paiement (« jamais accessProof pour cette route » pour `/api/membre/parrainage`).
- `accessProof` peut être **`null`** après connexion (secret de signature non configuré côté Worker). Ce n'est **pas une erreur** : la session reste valide pour le Worker Paiement. Les appels aux autres Workers renverront alors `ACCESS_PROOF_MISSING`.
- Communauté : l'en-tête est envoyé **sur tous les appels**, sans exception. Boutique/Marketplace : seulement sur les routes marquées « Auth » ci-dessous.

Intercepteurs OkHttp attendus (module `:core:network`) :
- `BearerInterceptor` (Paiement, si `@Auth`), `AccessProofInterceptor` (autres Workers, si `@Auth`), pas d'intercepteur pour le tracking token (en-tête explicite sur l'unique route concernée).
- `SessionExpiredInterceptor` : voir §1.5.

### 1.3 Trois styles de réponse (selon le Worker)

**Style A — Paiement** (pas d'enveloppe) :
- Succès : les champs sont **à la racine** du JSON (pas de `success`, pas de `data`).
- Erreur HTTP : corps `{ "erreur": "<message>" }` avec 400/401/404…
- **Piège** : certaines routes renvoient **HTTP 200 avec `{ "erreur": "..." }`** (ex. profil, détail retrait) ou **HTTP 200 avec `status: "refuse"`** (retrait, renouvellement, code cadeau). Il faut toujours lire le corps.

**Style B — Boutique, Marketplace, Communauté** (enveloppe universelle) :
```json
{ "success": true|false, "error": "<texte|null>", "code": "<CODE|null>", "data": { ... } | null }
```
- Communauté ajoute aussi `field` (ou `data.field`) et `retryAfterSeconds` (ou `data.retryAfterSeconds`).
- Les erreurs métier arrivent **avec leur statut HTTP** (401/403/404/409/422/429/502/503) **et** l'enveloppe. Le client ne lève pas d'exception pour une erreur métier : il lit `success`/`code`.
- Seules vraies exceptions : réseau (`NETWORK_ERROR`, status 0) et corps non-JSON (`INVALID_RESPONSE`).

**Style C — Contenu** (pas d'enveloppe, codes dans `error`) :
- Succès : objet ou `{ items, next_cursor }` à la racine.
- Erreur : HTTP ≠ 2xx avec corps `{ "error": "<CODE>" }` (voir §3.7).

### 1.4 Pagination (trois formes — ne pas harmoniser)

| Worker | Forme | Paramètres | Réponse |
|---|---|---|---|
| Contenu catalogue | **Curseur** | `limit` (défaut 30, max 100), `cursor_created_at`, `cursor_id` | `{ items, next_cursor }` ; fin = `items: []` + `next_cursor: null` (jamais une erreur) |
| Contenu recherche | **Offset** | `limit` (défaut 20), `offset` (défaut 0) | `{ query, items, limit, offset }` |
| Paiement paiements | **Page** | `page` (défaut 1), `limit` (défaut 20, plafond 100) | `{ paiements, page, limit, total, totalPages }` — règles appliquées par le serveur, ne pas les dupliquer |
| Communauté messages / support | **Temps** | `limit` (défaut 50, plafond 100), `before` (epoch ms) | `{ items }` chronologique croissant, **ne jamais re-trier** |
| Autres listes | aucune | — | listes plafonnées par le serveur (50/100/200 selon route) |

Curseur : le service du site transmet `cursor_created_at` et `cursor_id` repris **du dernier élément reçu** (contrat : « curseur du dernier élément »). **Aucun écran du site ne pagine encore par curseur** : la forme exacte de l'objet `next_cursor` est donc **[À CONFIRMER]** avec le Worker Contenu avant d'implémenter le défilement infini.

### 1.5 Session expirée (une seule gestion, centralisée)

Sur le site, chaque service teste `401` et renvoie `sessionExpired`. **Dans l'app, c'est interdit dans les écrans et les repositories** :
- Un `SessionExpiredInterceptor` détecte : HTTP 401 sur le Worker Paiement, **ou** `code ∈ {ACCESS_PROOF_EXPIRED, ACCESS_PROOF_INVALID, ACCESS_PROOF_INVALID_ISSUER, ACCESS_PROOF_MALFORMED, ACCESS_PROOF_INVALID_SIGNATURE, ACCESS_PROOF_MALFORMED_PAYLOAD, ACCESS_PROOF_WRONG_ISSUER}`.
- Il purge la session (`SessionStore`) et émet un événement global `SessionEvent.Expired` → la navigation renvoie à `connexion`.
- `ACCESS_PROOF_MISSING` = pas de preuve **(à traiter comme session expirée si l'utilisateur est censé être connecté, sinon invitation à se connecter)**.
- Attention : `SUBSCRIPTION_INACTIVE` / `ACCESS_SUBSCRIPTION_INACTIVE` **n'est pas** une expiration de session : c'est un abonnement inactif (écran dédié / invitation à renouveler).

### 1.6 Codes d'erreur transverses → `AppError` (module `:core:common`)

| Code reçu | `AppError` | Message utilisateur (clé `strings.xml`) |
|---|---|---|
| `NETWORK_ERROR` (réseau / timeout) | `Network` | « Erreur réseau. Vérifiez votre connexion. » |
| `INVALID_RESPONSE` (corps non-JSON) | `InvalidResponse` | « Réponse invalide du serveur. » |
| 401 Paiement, `ACCESS_PROOF_*` (expiré/invalide) | `SessionExpired` | « Votre session a expiré. Reconnectez-vous. » |
| `ACCESS_PROOF_MISSING` | `NotLoggedIn` | « Vous devez être connecté… » |
| `SUBSCRIPTION_INACTIVE`, `ACCESS_SUBSCRIPTION_INACTIVE` | `SubscriptionInactive` | « Votre abonnement doit être actif… » |
| `ACCOUNT_ACCESS_DENIED` (Boutique) | `AccountDenied` | « Votre compte n'est pas autorisé… » |
| `PROFILE_INCOMPLETE` (Communauté) | `CommunityProfileRequired` | → écran de création de profil |
| `RATE_LIMITED` / HTTP 429 | `RateLimited(retryAfterSeconds)` | pause des sondages (voir §6.0) |
| `NOT_FOUND` / HTTP 404 | `NotFound` | selon écran |
| `VALIDATION` | `Validation(field?)` | selon écran, `field` désigne le champ fautif |
| `FORBIDDEN`, `ACCESS_DENIED` | `Forbidden` | selon écran |
| `CONFLICT` | `Conflict` | selon écran |
| `UPSTREAM`, `UNAVAILABLE`, `MEDIA_SERVICE_UNAVAILABLE` | `ServiceUnavailable` | « Service temporairement indisponible. » |
| autre | `Unknown(code, message?)` | message générique par domaine |

Les messages précis par route sont listés dans chaque section : ils deviennent des entrées `strings.xml` (le texte du site est repris tel quel en FR).

### 1.7 Idempotence

| Où | Mécanisme | Règle |
|---|---|---|
| Boutique `POST /api/shop/checkout` | `idempotencyKey` (corps) = UUID v4 | **Nouvelle clé par tentative distincte.** Réutiliser la même clé uniquement pour relire le résultat d'un checkout déjà envoyé. |
| Marketplace `POST /api/marketplace/orders` | en-tête `Idempotency-Key: chk_<uuid sans tirets>` | Générée **après** confirmation d'achat. Conservée pour retry/rafraîchissement de la même intention (clé `dsi_mp_checkout_<listingId>` → `{key, listingId, at}`, stockage temporaire ; sur Android : `DataStore`/mémoire de session, supprimée à la fin). |
| Marketplace upload candidature | champ `idempotencyKey` (multipart, optionnel) | — |
| Communauté support `POST /api/community/support/messages` | `messageId` (corps) = UUID | stable par tentative, nouveau par message |

---

## 2. Worker PAIEMENT (`PAYMENT`) — style A

Auth : Bearer sauf mention. Routes : **14**.

### 2.1 `POST /api/inscription` — Public
Crée la demande d'inscription (compte + checkout paiement). **Aucune session n'est renvoyée.**

Requête :
```json
{ "nom": "", "prenom": "", "whatsapp": "", "email": "", "pays": "", "formule": "acces_initial",
  "codeParrain": "", "motDePasse": "" }
```
- `formule` : constante `"acces_initial"`. `prenom` et `codeParrain` : chaîne vide si absents.

Réponse succès : `{ "userId", "status": "pending_payment", "paymentUrl", "paymentTrackingToken" }`
Réponse erreur métier : `{ "erreur": "<message>" }` (HTTP possiblement 200) → à afficher tel quel.
Erreurs : réseau → « La création de votre compte a échoué. Vérifiez votre connexion et réessayez. »

### 2.2 `GET /api/paiement/statut` — En-tête `X-Payment-Tracking-Token`
Statut du paiement d'inscription. Pas de Bearer. Token expiré sous 24 h, inutilisable une fois le paiement confirmé actif (la session s'obtient alors par une nouvelle connexion).
Réponse : objet à la racine avec un champ **`status`** de valeur **`attente`** (paiement pas encore confirmé → continuer d'attendre), **`active`** (paiement confirmé, compte actif ; **aucune session n'est délivrée** : l'utilisateur doit se reconnecter), **`refuse`** ou **`introuvable`** (échec → proposer de refaire la demande). Autres champs éventuels : **[À CONFIRMER]**.
Erreur réseau → « Impossible de vérifier votre paiement pour le moment. »

### 2.3 `POST /api/auth/connexion` — Public
Requête : `{ "identifiant": "<email ou téléphone>", "motDePasse": "" }`

Réponse A — compte actif :
```json
{ "status": "connecte", "authenticated": true, "userId": "", "accountStatus": "",
  "subscriptionStatus": "", "subscriptionActive": true, "dateFin": "", "token": "",
  "expiresIn": 0, "accessProof": "<peut être null>" }
```
Réponse B — paiement en attente :
```json
{ "status": "pending_payment", "userId": "", "paymentTrackingToken": "", "paymentUrl": "" }
```
(aucun `token`, aucun `accessProof`, aucun `authenticated`). Le client enregistre un **brouillon d'inscription** (`userId`, `paymentTrackingToken`, `paymentUrl`, `etape: "paiement"`) et reprend le parcours d'inscription **à l'étape paiement**, sans recréer de compte.
Réponse C — échec : message **volontairement générique** du Worker (`message`) — ne jamais révéler si l'identifiant ou le mot de passe est en cause.
Session enregistrée : `token`, `accountStatus`, `subscriptionStatus`, `subscriptionActive`, `dateFin`, `accessProof`.
Option « Se souvenir de moi » : oui = stockage persistant, non = mémoire de session (effacée à la fermeture).

### 2.4 `GET /api/membre/profil` — Bearer
Succès : champs du profil **à la racine** (pas de `success`). L'écran Profil du site accepte **plusieurs noms pour le même champ** (le front ne sait pas lequel le Worker envoie). Le DTO doit donc porter **tous les alias** et le mapper choisit le premier non vide :

| Donnée | Alias acceptés (dans l'ordre) |
|---|---|
| Prénom | `prenom`, `first_name` |
| Nom | `nom`, `last_name` |
| Identifiant DSI | `identifiantDsi`, `id_dsi` |
| Niveau | `niveauActuel`, `niveau` (défaut : « Membre ») |
| Code de parrainage | `codeParrainagePersonnel`, `codeParrainage`, `code_parrainage` |
| Statut du compte | `statut`, `statut_compte` (défaut `ACTIF` ; actif si `actif`/`active`, insensible à la casse) |
| Date d'inscription | `dateInscription`, `created_at` |
| Date d'activation | `dateActivation`, `activated_at` |
| Téléphone | `whatsapp`, `telephone` |
| Autres | `email`, `pays` |

Donnée absente = « non renseigné », jamais un faux zéro.
Erreur métier possible **en HTTP 200** : `{ "erreur": "..." }`. 401 : session expirée.
Messages : « Impossible de récupérer votre profil pour le moment. Vérifiez votre connexion et réessayez. »

### 2.5 `GET /api/membre/solde` — Bearer
```json
{ "identifiantDsi": "", "soldeDsiActuel": "<string|number>", "totalDsiGagnes": "<string|number>", "dateMiseAJour": "" }
```
**Les montants peuvent être des chaînes ou des nombres** → DTO en `JsonElement`, conversion en `Double` (défaut 0) **dans le mapper, une seule fois**.

### 2.6 `GET /api/membre/abonnement` — Bearer
Champs lus par les écrans : `statut`, `dateDebut`, `dateFin`, `formule`, `modePaiement`, `renouvellements`. Règle d'état affichée (à reprendre) : si `dateFin` est valide → **actif** si `dateFin` > maintenant, sinon **expiré** ; sans `dateFin` valide → **actif** si `statut === "active"`, sinon **aucun**. Autres champs : **[À CONFIRMER]**.

### 2.7 `POST /api/abonnement/renouveler` — Bearer
Requête : `{ "modePaiement": "fcfa" | "dsi" }`
Réponses (toutes **sans champ `success`**) :
- FCFA : `{ "status": "pending_payment", "paymentUrl" }`
- DSI succès : `{ "status": "active", "message" }`
- DSI refus (solde insuffisant / fenêtre fermée) : `{ "status": "refuse", "message" }`
Tarif d'affichage : `RENEWAL_PRICE_DSI = 2500` (constante d'affichage uniquement).

### 2.8 `GET /api/membre/retraits` — Bearer
```json
{ "total": 0, "retraits": [ { "idRetrait", "statut", "moyenPaiement", "montantDsiDemande", "dateDemande",
  "pays", "montantFcfa", "montantDsiNet", "contributionStabiliteDsi" } ] }
```
Champs observés dans les écrans. **Statuts de retrait** (`statut`) : `demande` → `validee` → `envoyee` → `payee` (ordre des étapes de la frise) ou `rejetee` ; tout statut inconnu s'affiche tel quel, avec le style « en attente ». (Un composant du tableau de bord teste aussi `statut === "traite"` : incohérence du site, **[À CONFIRMER]** si cette valeur existe.) Taux d'**affichage** de l'estimation FCFA : `0,4` FCFA par DSI — constante d'affichage ; les montants réels viennent de `montantFcfaNet`. Si `retraits` n'est pas un tableau → liste vide.

### 2.9 `GET /api/retrait/moyens-disponibles?pays=XX` — Public
`pays` optionnel (le Worker applique « AUTRE » si absent/inconnu) : **code ISO 3166-1 alpha-2** (le site propose 196 pays, défaut `BF`). Réponse : `{ "pays": "XX", "moyens": [ "<libellé>", ... ] }` — **chaque moyen est une simple chaîne** (le libellé ET la valeur envoyée dans `moyenPaiement`). Le champ de saisie du numéro s'adapte au libellé (affichage uniquement ; le corps envoyé reste toujours un seul champ texte `numeroPaiement`) : `carte|visa|mastercard`→carte ; `virement|iban|rib|swift|compte bancaire`→banque ; `money|momo|wave|mobile`→mobile (clavier téléphone) ; sinon texte.
Erreur : « Impossible de récupérer les moyens de paiement disponibles. »

### 2.10 `POST /api/retrait/demander` — Bearer
Requête : `{ "montantDsi": <number>, "moyenPaiement": "<string>", "numeroPaiement": "<string>" }` — **jamais d'`identifiantDsi`/`userId`**.
Réponse (**toujours HTTP 200**, sans `success`) :
- `{ "status": "demande_enregistree", "idRetrait", "montantDsiNet", "montantFcfaNet", "message" }`
- `{ "status": "refuse", "message" }` → afficher le message du Worker tel quel.
Validation client = confort UX (champs non vides) uniquement ; **ne jamais** recoder plafond, jours, fréquence.

### 2.11 `GET /api/membre/retraits/{idRetrait}/historique` — Bearer
`{ "idRetrait", "evenements": [ { "date", "champ", "ancienneValeur", "nouvelleValeur" } ] }`
Erreur `{ "erreur" }` en 200 possible. Le Worker renvoie le **même message** si le retrait n'existe pas ou appartient à un autre membre : ne jamais distinguer les deux cas.

### 2.12 `GET /api/membre/paiements?page=X&limit=Y` — Bearer
```json
{ "paiements": [ { "idTransaction", "typePaiement", "statut": "pending|paid", "referenceExterne",
  "plateforme", "montant", "devise", "dateCreation", "dateConfirmation" } ],
  "page": 1, "limit": 20, "total": 0, "totalPages": 0 }
```
`statut` : **uniquement `pending` ou `paid`**. 404 → « Compte introuvable. »

### 2.13 `GET /api/membre/dsi/historique` — Bearer — **NON UTILISÉE en v1**
Aucun écran du site ne l'appelle (l'écran « Mon DSI » affiche : « historique détaillé, en attente d'une route réelle »). **Ne pas l'implémenter** tant que le Worker n'en confirme pas la forme.

### 2.14 `GET /api/membre/parrainage` — Bearer (jamais Access Proof)
```json
{ "codeParrainagePersonnel": "", "filleulsDirectsTotal": 0, "filleulsDirectsActifs": 0,
  "recompensesNiveau1Dsi": 0, "recompensesNiveau2Dsi": 0, "totalDsiParrainage": 0,
  "historique": [ { "type", "montant", "identifiantDsiFilleul", "date" } ], "historiqueHasMore": false }
```
Membre sans filleul = même structure à zéro + historique vide : **état vide normal, jamais des données fictives**. 404 → « Compte introuvable. »
Récompenses affichées (règles) : N1 = 500 DSI, N2 = 120 DSI (affichage seul).

### 2.15 `POST /api/dsi/utiliser-code` — Bearer
Requête : `{ "code": "" }` (identité = token).
- 401 `{erreur}` → session ; 400 `{erreur}` → code vide.
- **HTTP 200 dans tous les autres cas** : lire `status`.
  - Succès : `{ "status": "succes", "montantDsi": <number>, "message" }` (le montant vient toujours du Worker).
  - Refus : message générique « Ce code est invalide ou a expiré. » pour tous les cas, sauf « Vous avez déjà utilisé ce code. » (détecté par le texte `déjà utilisé`, insensible à la casse).
- Le Repository **renvoie une raison** (`Success(montant)` ou `Failure(reason)`), pas un texte : `alreadyUsed`, `invalid`, `session`, `empty`, `unavailable`. L'écran traduit.

*(Total Paiement : 15 routes numérotées 2.1 à 2.15.)*

---

## 3. Worker CONTENU (`CONTENT`) — style C

Auth : **aucune pour la découverte** ; `X-Access-Proof` pour `deliver`. Aucune route de ce domaine n'utilise le Bearer.

### 3.1 Catalogue — `GET /content`
Query : `type` (`book|video|audio|training`), `category_id`, `limit`, `cursor_created_at`, `cursor_id`.
Réponse : `{ "items": [ContentItem], "next_cursor": {...}|null }`.

Correspondance maison → `type` : Bibliothèque=`book`, Vidéothèque=`video`, Audiothèque=`audio`, Formations=`training`.

### 3.2 Catégories — `GET /content/categories?type=<type>`
`{ "items": [ { "id", "name", "slug", "count" } ] }` — jamais de catégorie vide (jointure serveur).

### 3.3 Recherche — `GET /search?q=&type=&category_id=&limit=&offset=`
`{ "query", "items", "limit", "offset" }` (offset, pertinence).

### 3.4 Fiche — `GET /content/{id}` et `GET /content/by-slug/{type}/{slug}`
Objet `ContentItem`. Erreur : 404 `CONTENT_NOT_FOUND`. (`by-slug` existe pour `book`, `video`, `audio` ; **pas pour `training`**.)

**ContentItem — champs observés dans les écrans** : `id`, `type`, `title`, `description`, `thumbnail_url`, `duration_seconds`, `created_at`, `level` (**liste complète [À CONFIRMER]** ; clés en `snake_case`. Le Worker accepte un `slug` — route `by-slug` — mais ce champ n'est lu par aucun écran du site).
La progression (`progress`) **n'existe pas côté serveur** : elle est locale (voir §8).

### 3.5 Meilleur asset — `GET /content/{content_id}/assets/best`
Contient le champ `downloadable`. **404 (`NO_AVAILABLE_ASSET`) n'est pas une erreur** : « rien à livrer », `downloadable = false`, aucun message.
Le seul champ lu par le site est `downloadable` (booléen). Autres champs de l'asset : **[À CONFIRMER]**. Valable pour tout type ; pour une formation on l'appelle sur l'**id du module enfant**, jamais sur l'id de la formation.

### 3.6 Formations — structure
`GET /content/{id}/components/tree` →
```json
{ "...champs de la formation...", "children": [ { "component": {...}, "content": ContentItem, "children?": [...] } ] }
```
Récursif. Un nœud lu par l'arborescence : `content` = `{ id, title, type, duration_seconds, children? }` (le programme s'affiche en liste imbriquée ; chaque module a son `id`). **Règle critique** : une formation n'a **jamais** d'asset propre. `deliver` ne s'appelle **jamais** avec l'id d'une formation : toujours avec `content.id` d'un **module enfant**. (Existe aussi `/content/:parent_id/components`, non utilisé par le site.)

### 3.7 Livraison — `GET /content/{id}/deliver[?intent=download]`
En-têtes : `X-Access-Proof` (**obligatoire**, sinon le client renvoie `ACCESS_PROOF_MISSING` sans appel réseau), `Range` optionnel (ex. `bytes=0-`).
`intent=download` exige `download_allowed=1` sur l'asset ET la capacité « download » du fournisseur.

Réponses (détectées par **Content-Type**, jamais devinées) :

| Forme | Condition | Contenu | Kotlin |
|---|---|---|---|
| **video_token** | `application/json` avec `type:"video_playback_token"` et `playback_url` | `{ status:"OK", type, content_id, playback_url, expires_at }` | `DeliveryResult.VideoToken(playbackUrl = CONTENT + playback_url, expiresAt)` → donné tel quel à ExoPlayer, qui fait lui-même les requêtes Range |
| **book_token** | `application/json` avec `type:"book_playback_token"` et `read_url` (**PDF uniquement**) | `{ status:"OK", type, content_id, read_url, expires_at }` | `DeliveryResult.BookToken(readUrl = CONTENT + read_url, expiresAt)` → lecteur PDF en mode Range HTTP |
| **json** | `application/json`, sinon | `{ content_id, content_type, asset_id, provider_id, format, delivery:{ status:"OK", url } }` — l'URL est dans `delivery.url`, jamais à la racine | `DeliveryResult.Url(url, format)` (livres epub/mobi/docx/txt, audio, YouTube) |
| **stream** | autre Content-Type | flux binaire, 200 ou 206, `Content-Range`/`Accept-Ranges`, `Content-Disposition: ...filename="..."` (nom de fichier à extraire) | `DeliveryResult.Stream(...)` — **ne pas charger en mémoire** : écrire sur disque (téléchargement) ou streamer |

`playback_url` / `read_url` sont **relatifs** : le client les préfixe par l'URL de base Contenu. Ces URLs sont **temporaires** (jamais en cache disque), vérifiées par le Worker à chaque requête Range.

Erreurs (`{"error":"CODE"}`) — codes réels du contrat (section F) :

| Code | HTTP | Message FR (site) |
|---|---|---|
| `ACCESS_PROOF_MISSING` | 401 | Accès non disponible. Reconnectez-vous ou vérifiez votre abonnement. |
| `ACCESS_PROOF_MALFORMED` / `_INVALID_SIGNATURE` / `_MALFORMED_PAYLOAD` / `_WRONG_ISSUER` | 401 | Votre preuve d'accès est invalide. Reconnectez-vous. |
| `ACCESS_PROOF_EXPIRED` | 401 | Votre session d'accès a expiré. Reconnectez-vous. |
| `ACCESS_SUBSCRIPTION_INACTIVE` | 403 | Votre abonnement ne permet pas de consulter ce contenu. |
| `CONTENT_NOT_FOUND` | 404 | Ce contenu est introuvable. |
| `CONTENT_NOT_AVAILABLE` | 403 | Ce contenu n'est plus disponible. |
| `NO_COMPATIBLE_ASSET` | 404 | Ce contenu n'est pas disponible pour le moment. |
| `NO_DOWNLOADABLE_ASSET` | 404 | Ce contenu n'est pas téléchargeable. |
| `NO_DELIVERABLE_ASSET` | 404 | Ce contenu n'a pas pu être livré pour le moment. |
| `ACCESS_PROOF_PUBLIC_KEY_NOT_CONFIGURED`, `*_SECRET_NOT_CONFIGURED` | 500 | Service temporairement indisponible. |
| `VIDEO_TOKEN_MALFORMED` / `_INVALID_SIGNATURE` / `_WRONG_PURPOSE` / `_CONTENT_MISMATCH`, `BOOK_TOKEN_*` (idem), `AMBIGUOUS_TOKEN` | 401/403/400 | Votre session de lecture est invalide. Rechargez. |
| `VIDEO_TOKEN_EXPIRED`, `BOOK_TOKEN_EXPIRED` | 401 | Votre session de lecture a expiré. Rechargez. |
| `VIDEO_DURATION_UNKNOWN`, `BOOK_SIZE_UNKNOWN` | 422 | Ce contenu n'est pas encore disponible en lecture. |
| (corps non-JSON ou code inconnu) | — | « Impossible de récupérer ce contenu pour le moment. » (`DELIVERY_ERROR`) |
| (réseau) | — | `NETWORK_ERROR` : « Erreur réseau pendant la consommation du contenu. » |

Erreurs de téléchargement (clés d'affichage) : `NO_DOWNLOADABLE_ASSET`→« pas téléchargeable » ; `ACCESS_SUBSCRIPTION_INACTIVE`→« accès refusé » ; HTTP 403 / `DOWNLOAD_NOT_ALLOWED` / `DOWNLOAD_FORBIDDEN`→« téléchargement non autorisé ». **Le bouton Télécharger est toujours visible : c'est le Worker qui décide.**

**Sur Android** : le téléchargement sort de `deliver(intent=download)` → `WorkManager` écrit dans le stockage de l'app (pas de Blob/`createObjectURL`). Si `kind=json` avec `url`, ouvrir/télécharger l'URL.

---

## 4. Worker BOUTIQUE (`SHOP`) — style B

Catégorie A = public. Catégorie B = **Auth** (`X-Access-Proof`). Routes : **17**.

Erreurs d'authentification communes (Catégorie B) :
`ACCESS_PROOF_MISSING` (401), `ACCESS_PROOF_INVALID`, `ACCESS_PROOF_INVALID_ISSUER`, `ACCESS_PROOF_EXPIRED`, `ACCOUNT_ACCESS_DENIED` (« Votre compte n'est pas autorisé à accéder à la Boutique. »).

### Catalogue (public)
| # | Route | Détails |
|---|---|---|
| 4.1 | `GET /api/shop/catalog?category=&q=` | `category` : absent ou `all` = pas de filtre. Tri serveur : `featured DESC, updated_at DESC`, **200 max**. `data: { items }`. Vide = `items: []`. |
| 4.2 | `GET /api/shop/catalog/{id}` | `data` = produit. 404 `NOT_FOUND` = inexistant **ou** non publié (aucune distinction, volontaire). |
| 4.3 | `GET /api/shop/categories` | `data: { categories: [ { category, count } ] }` — uniquement les produits publiés. |

Champs produit observés : `id`, `title`, `tagline`, `description`, `category`, `badge`, `featured`, `priceDsi`. Autres champs : **[À CONFIRMER]**.
Commande observée : `{ id, status, createdAt, items: [...], totalDsi }` ; `items` d'une commande et lignes de panier : `{ productId, qty, … }`. Bibliothèque : `{ productId, title, category, assetFileId, hasAsset }`.

### Panier (Auth)
| # | Route | Corps / réponse |
|---|---|---|
| 4.4 | `GET /api/shop/cart` | `data: { items }`. Effet de bord serveur : auto-nettoyage silencieux des produits supprimés. |
| 4.5 | `PUT /api/shop/cart` | `{ "items": [ { "productId": "", "qty": 1 } ] }` — **remplace** tout le panier. Aucune vérification d'existence ici. |
| 4.6 | `POST /api/shop/cart/items` | `{ "productId": "", "qty": 1 }` (qty défaut 1, min 1 ; **s'additionne** si déjà présent). Erreurs : `VALIDATION` (« Produit invalide. »), `PRODUCT_NOT_FOUND`, `PRODUCT_ARCHIVED` / `PRODUCT_NOT_PUBLISHABLE` (« n'est plus disponible à l'achat »). |
| 4.7 | `DELETE /api/shop/cart/items/{productId}` | Silencieux si absent. `data: { items }`. |

Le panier renvoyé par 4.4–4.7 est toujours `data.items` ; élément observé : `{ productId, qty, … }`.

### Checkout & commandes (Auth)
| # | Route | Détails |
|---|---|---|
| 4.8 | `POST /api/shop/checkout` | Corps : `{ "idempotencyKey": "<uuid>", "promoCode"?: "", "retryOfOrderId"?: "" }` (`retryOfOrderId` informatif). **HTTP 200** = résultat terminal (`status:"paid"` le plus souvent, ou statut déjà connu si clé rejouée). **HTTP 202** = en cours/indéterminé : `status ∈ {processing, validating, pending_payment, payment_unknown}` → **relire via 4.10 après un court délai ; ne jamais renvoyer un checkout avec une nouvelle clé tant que le statut n'a pas évolué.** `data` = commande (champ `status` lu par le site ; si `paid`, l'écran affiche le succès). |
| 4.9 | `GET /api/shop/orders` | 50 plus récentes. `data: { orders }`. Effet de bord : réconciliation auto des `payment_unknown`, réparation des droits manquants sur `paid` — **jamais de nouveau débit**. |
| 4.10 | `GET /api/shop/orders/{id}` | Détail (inclut `failureReason` si `failed`, et `paymentRef`). 404 `NOT_FOUND` = inexistante ou pas au membre. |

Erreurs checkout : `VALIDATION` (« Votre panier ou la demande d'achat est invalide. »), `PRODUCT_NOT_FOUND`, `PRODUCT_ARCHIVED`, `PRODUCT_NOT_PUBLISHABLE`, `CONTENT_FILE_REQUIRED` (« n'a pas de fichier associé »), `PAYMENT_RECONCILIATION_PENDING` (« Une opération précédente est encore en cours de vérification. Réessayez dans un instant. »), `UPSTREAM` (« Le paiement n'a pas pu être traité pour le moment. »).
Statuts de commande connus : `paid`, `processing`, `validating`, `pending_payment`, `payment_unknown`, `failed`.

### Favoris (Auth)
| # | Route | Détails |
|---|---|---|
| 4.11 | `GET /api/shop/favorites` | `data: { productIds: [...] }` |
| 4.12 | `POST /api/shop/favorites` | `{ "productId": "" }` — idempotent. `data: { productId }` |
| 4.13 | `DELETE /api/shop/favorites/{productId}` | idempotent. `data: { productId }` |

### Promo (Auth)
| # | Route | Détails |
|---|---|---|
| 4.14 | `POST /api/shop/promo/validate` | `{ "code": "" }` → `data: { code, type, value }` (`type = "percent"` s'affiche `-{value}%` ; autre type : valeur affichée telle quelle). **Indicatif uniquement, ne consomme aucun quota** : la validation réelle a lieu au checkout (un code valide ici peut être refusé ensuite). 404 `NOT_FOUND` = « Ce code promo n'existe pas. » ; 409 `CONFLICT` = désactivé / pas encore actif / expiré / épuisé / limite personnelle atteinte (« n'est plus utilisable »). |

### Bibliothèque d'achats & fichiers (Auth)
| # | Route | Détails |
|---|---|---|
| 4.15 | `GET /api/shop/library` | `data: { items }`. Chaque item a `assetFileId` (version **réellement acquise**, snapshot) et `hasAsset`. **`hasAsset:false` = ressource indéterminée → ne proposer aucun téléchargement.** Ne jamais reconstruire `assetFileId`. |
| 4.16 | `POST /api/shop/media/{fileId}/read-url` | Corps `{}`. `fileId` = **uniquement** un `assetFileId` de 4.15. `data` = URL temporaire (**TTL 300 s**). **Forme exacte NON formalisée, même le site tente de la deviner** : il cherche dans cet ordre `data.url`, `data.readUrl`, `data.signedUrl`, `data.downloadUrl` ; si aucune ne correspond il affiche la réponse brute. L'app doit faire pareil (DTO avec les 4 champs optionnels) et **journaliser la réponse brute** au premier test réel pour fixer la forme. L'URL s'ouvre via `Intent` navigateur/visionneuse (jamais mise en cache). Erreurs : 403 `FORBIDDEN` / `ACCESS_DENIED` (« Vous n'avez pas accès à ce fichier. ») ; 409 `ENTITLEMENT_ASSET_UNAVAILABLE` (**ne jamais réessayer automatiquement**, « Contactez le support ») ; 503 `UNAVAILABLE` et 502 `UPSTREAM` (« Service de fichiers temporairement indisponible. »). |

*(Total Boutique : 16 routes, numérotées 4.1 à 4.16.)*

---

## 5. Worker MARKETPLACE (`MARKETPLACE`) — style B

Auth = `X-Access-Proof`. Le client Marketplace accepte aussi `multipart/form-data`. Routes : **33** (7 publiques + 9 membre + 17 vendeur).

Montants en **DSI** (`priceDsi`). Dates : le Worker peut envoyer **secondes Unix, millisecondes ou ISO** → tout convertir en millisecondes dans le mapper (règle du site : `n < 1e12` ⇒ ×1000).
Un champ absent donne une valeur vide, **jamais un faux zéro**. Gratuit : `free === true` (le Worker garantit `free` quand `priceDsi = 0`).

### 5.1 Public (catalogue, sans Auth)
| Route | Détails |
|---|---|
| `GET /api/marketplace/catalog?category=&q=&ids=a,b,c` | `ids` (≤100) renvoie ces produits en un appel, **dans l'ordre demandé**, mêmes règles de visibilité. |
| `GET /api/marketplace/catalog/{id}` | Fiche produit. |
| `GET /api/marketplace/catalog/{id}/reviews` | `{ reviews: [ { id, rating, comment, createdAt } ] }` (anonymes). |
| `GET /api/marketplace/trending` | Tendances. |
| `GET /api/marketplace/categories` | Catégories. |
| `GET /api/marketplace/sellers` | Vendeurs. |
| `GET /api/marketplace/sellers/{memberId}` | Fiche vendeur. |

Produit : `{ id, sellerId, title, description, category, priceDsi, free, coverUrl, storeName, reviewAvg, reviewCount, salesCount, createdAt }`.
Note moyenne affichée **seulement s'il existe au moins un avis** (`reviewAvg > 0` et `reviewCount ≠ 0`).

### 5.2 Membre (Auth)
| Route | Détails |
|---|---|
| `GET /api/marketplace/me/favorites` | Favoris. |
| `POST /api/marketplace/me/favorites` | `{ "listingId": "" }` |
| `DELETE /api/marketplace/me/favorites/{listingId}` | |
| `POST /api/marketplace/orders` | `{ "listingId": "" }` + en-tête **`Idempotency-Key`** (voir §1.7). Le paiement se fait en **solde DSI** (l'écran lit d'abord `GET /api/membre/solde` du Worker Paiement ; Mobile Money et virement sont présentés mais **verrouillés**). Réponse `data` : `{ orderId` (ou `id`), `status }`. Traitement : `paid` → appeler `delivery` ; `failed` → échec définitif, **effacer** l'intention d'achat ; `processing`, `pending_payment` ou statut inconnu → « en attente », bouton « vérifier mon paiement » (reconcile). |
| `GET /api/marketplace/me/orders` | Mes commandes : liste dans `data.orders` **ou** `data.items` (accepter les deux). |
| `GET /api/marketplace/me/orders/{orderId}` | Détail. |
| `GET /api/marketplace/me/orders/{orderId}/delivery` | `data: { deliveryUrl }` — lien de livraison (ouvert dans le navigateur) ; absent = « payé, sans lien ». |
| `POST /api/marketplace/me/orders/{orderId}/reconcile` | Corps `{}`. `data` : `{ status, … }`, rejoué dans le même traitement que la création. **Respecter ≥ 8 s entre deux vérifications** (le site impose ce délai côté interface). |
| `GET /api/marketplace/me/payment-history` | Liste dans `data.items` **ou** `data.orders` ; ligne : `{ orderId` (ou `id), listingId, listingTitle, priceDsi, free, status, createdAt }`. |

**Sens des codes d'erreur d'achat** (confirmés par `errorKeyFrom` du site) :

| Code | Clé d'affichage | Intention d'achat conservée pour un nouvel essai ? |
|---|---|---|
| `NETWORK_ERROR` | réseau | oui |
| `INVALID_RESPONSE` | réponse invalide | oui |
| `ACCESS_PROOF_INVALID`, `ACCESS_PROOF_EXPIRED`, HTTP 401 | session | non |
| `SUBSCRIPTION_INACTIVE` | abonnement | non |
| `NOT_FOUND` | introuvable | non |
| `LISTING_PRIVATE` | annonce privée | non |
| `SELF_PURCHASE_FORBIDDEN` | « votre propre produit » | non |
| `SELLER_UNAVAILABLE` | vendeur indisponible | non |
| `FORBIDDEN` | interdit | non |
| `INSUFFICIENT_BALANCE` | solde insuffisant | non |
| `PAYMENT_REJECTED` | paiement refusé | non |
| `UPSTREAM` | vérification en cours | **oui** |
| `CONFIG` | service occupé | **oui** |
| autre | inconnu | **oui** |

« Conservée » = réutiliser la **même** `Idempotency-Key` (retry sûr). Sinon, nouvelle clé au prochain essai.

Commande : `{ id, listingId, priceDsi, status, createdAt, listingTitle }` (+ côté vendeur `netSellerDsi`).
Statuts de commande observés : `paid`, `processing`, `pending_payment`, `rejected`, `expired`, `failed`.
Codes observés dans les écrans : `FORBIDDEN`, `ACCESS_PROOF_INVALID`, `ACCESS_PROOF_EXPIRED`, `SUBSCRIPTION_INACTIVE`, `VALIDATION`, `UPSTREAM`, `UNAVAILABLE`, `NOT_FOUND`, `NETWORK_ERROR`, `INVALID_RESPONSE`, `SELLER_UNAVAILABLE`, `SELF_PURCHASE_FORBIDDEN`, `LISTING_PRIVATE`, `PAYMENT_REJECTED`, `INSUFFICIENT_BALANCE`. **Le sens précis de chaque code par route : [À CONFIRMER] avec le contrat du Worker** (le site les interprète dans les pages, pas dans les services).

### 5.3 Vendeur (Auth)
| Route | Corps / détails |
|---|---|
| `GET /api/marketplace/seller/application` | `data: { status, … }` avec `status ∈ none | PENDING | APPROVED | REJECTED | SUSPENDED | REVOKED`. Après un `REJECTED`, le Worker renvoie `storeName`, `slogan`, `description`, `logoFileId` (pré-remplissage) ; **les pièces d'identité ne sont jamais renvoyées : à renvoyer.** |
| `POST /api/marketplace/seller/application` | **7 champs obligatoires** : `{ "storeName": "≤80", "slogan": "≤120", "description": "≤2000", "logoFileId", "idDocumentFrontFileId", "idDocumentBackFileId", "idSelfieFileId" }`. Les `*FileId` viennent de l'envoi de fichiers ci-dessous. Erreurs : 401/403 session, 409 `conflict` (candidature déjà existante → recharger l'état), 422 invalide, ≥500 indisponible. |
| `POST /api/marketplace/seller/application/files` | **multipart** : `kind` ∈ `logo | id_front | id_back | id_selfie`, `file` (nom par défaut `<kind>.jpg`), `idempotencyKey?`. **8 Mo max.** Réponse `data: { fileId, kind, private: true }`. |
| `POST /api/marketplace/seller/application/resubmit` | Uniquement après `REJECTED`. Même payload (7 champs). |
| `GET /api/marketplace/seller/dashboard` | `data: { products, orders, revenueDsi }` (nombres). |
| `GET /api/marketplace/seller/portfolio` | **[À CONFIRMER]** (non lu par les écrans consultés). |
| `GET /api/marketplace/seller/listings` | Mes annonces (même forme que produit + `status`, `visibility`, `reason`). |
| `GET /api/marketplace/seller/listings/pending` | Annonces en attente de validation ; le compte (`pendingCount`) alimente l'avis « N produit(s) en attente ». |
| `POST /api/marketplace/seller/listings` | Création : `{ "name": "<titre>", "title": "<titre>", "description", "category", "priceDsi": <number>, "deliveryType": "URL", "deliveryUrl": "<lien>", "visibility": "PUBLIC"|"PRIVATE" }` (**`name` et `title` envoyés tous deux avec la même valeur**). `deliveryType` vaut toujours `"URL"` côté site. |
| `PATCH /api/marketplace/seller/listings/{id}` | `{ "title", "description", "category" }` + `priceDsi` **sauf si l'annonce est gratuite** (le prix d'une annonce gratuite est verrouillé : ne pas l'envoyer). |
| `DELETE /api/marketplace/seller/listings/{id}` | **Archive** (pas de suppression définitive). |
| `GET /api/marketplace/seller/orders` | Commandes reçues : `{ id, listingId, priceDsi, free, status, createdAt, netSellerDsi }` ; le titre vient de la liste des annonces (jointure par `listingId`). |
| `GET /api/marketplace/seller/orders/{id}` | Détail. |
| `GET /api/marketplace/seller/earnings` | `data: { availableDsi, ledger: [ { id, amountDsi, createdAt } ] }`. |
| `GET /api/marketplace/seller/analytics` | `data: { topListings: [ { id, title, sales_count` ou `salesCount } ] }` (5 premiers affichés). **Pas d'autres statistiques** : le site renvoie l'ancienne page « Statistiques » vers l'accueil vendeur. |
| `GET /api/marketplace/seller/settings` | `storeName`, `bio`, (+ `logoUrl`) **[reste À CONFIRMER]**. |
| `PATCH /api/marketplace/seller/settings` | **Seuls `storeName` et `bio` sont modifiables** après approbation. |

Statuts vendeur observés : `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`, `REVOKED`. Statuts d'annonce : `pending_review`, `published`, `suspended`, `archived`. Visibilité : `PUBLIC`/`PRIVATE`.

*(Total Marketplace : 7 + 9 + 17 = 33 routes.)*

---

## 6. Worker COMMUNAUTÉ (`COMMUNITY`) — style B

**Toutes** les routes envoient `X-Access-Proof`. Pas d'autre mécanisme (ni Bearer, ni `X-DSI-User-Id`, ni `userId` en corps/query).
Garde commune (codes) : `ACCESS_PROOF_MISSING`, `ACCESS_PROOF_INVALID`, `ACCESS_PROOF_EXPIRED`, `SUBSCRIPTION_INACTIVE` (« Votre abonnement doit être actif pour accéder à la Communauté. »), `PROFILE_INCOMPLETE` (« Vous devez d'abord créer votre profil communautaire. »).
Version du contrat : Worker Communauté v1.8.x.

### 6.0 Limitation de débit
HTTP 429 ou `code: "RATE_LIMITED"` (par membre et par minute). Le délai est dans **`retryAfterSeconds`** (corps de réponse ; défaut 30 s si absent). Pendant ce délai, **les sondages (polling) se mettent en pause**. Sur Android : un `RateLimitGate` dans `:core:network` + FCM à la place du polling quand c'est possible.

### 6.1 Domaine 1 — Conversations
| Route | Détails |
|---|---|
| `GET /api/community/conversations` | Triées par dernière activité, **plafond 100**, pas de curseur. `data: { items }`. |
| `POST /api/community/conversations` | `{ "type": "direct"|"group", "memberIds": [..], "title"?: "" }`. `direct` = exactement 1 `memberId` ; `group` = plusieurs + `title` requis. `memberIds` = `userId` DSI ou `@pseudonyme`. Réponse `{ id, existing, title }` — **si la DM existe déjà elle est renvoyée (`existing:true`), jamais une erreur.** Erreurs : `VALIDATION`, `MEMBER_NOT_IN_COMMUNITY`, `CONTACT_REQUIRED`, `INTERACTION_UNAVAILABLE`. |
| `GET /api/community/conversations/{id}/messages?limit=&before=` | Chronologique croissant (ne pas re-trier). Erreur `ACCESS_DENIED` (pas membre). |
| `POST /api/community/conversations/{id}/messages` | `{ "text"?: "≤4000", "attachmentFileId"?: "≤80" }` — au moins un des deux. Pour une DM, le contact est **re-vérifié à l'envoi**. Réponse `{ id, createdAt }`. Erreurs : `VALIDATION` (message vide), `CONTACT_REQUIRED`, `INTERACTION_UNAVAILABLE` (blocage), `NOT_FOUND`. |

Champs observés : conversation `{ id, type, title, peer, spaceId, lastMessage, lastMessageAt, status }` ; message `{ id, authorId, userId, pseudonym, displayName, text/body, attachmentFileId, createdAt }` (**les deux clés `text` et `body` apparaissent côté lecture → [À CONFIRMER]**).

### 6.2 Domaine 2 — Espaces
| Route | Détails |
|---|---|
| `GET /api/community/spaces?q=` | **Publics uniquement**, tri par nombre de membres décroissant, plafond 100. |
| `POST /api/community/spaces` | `{ "name": "≤80", "description"?: "≤500", "visibility"?: "public"|"private" }` (défaut `public`). Crée l'espace **et** sa conversation ; le créateur est owner des deux. Réponse `{ id, conversationId }`. |
| `GET /api/community/spaces/{id}` | Privé et non-membre → `ACCESS_DENIED` (« Cet espace est privé. »). 404 `NOT_FOUND`. |
| `POST /api/community/spaces/{id}/join` | Idempotent : `{ joined:true, already:true }`. Erreurs : `ACCESS_DENIED`, `INTERACTION_UNAVAILABLE`. |
| `POST /api/community/spaces/{id}/leave` | Le **propriétaire** ne peut pas partir (`ACCESS_DENIED`, « Transférez la propriété avant de quitter cet espace. »). |

Espace observé : `{ id, name, description, visibility, memberCount, joined, ownerId, conversationId }`.

### 6.3 Domaine 3 — Fil d'activité
`GET /api/community/activity` — fil **global** (non filtré), **50** derniers événements (`space.created`, `space.joined`). Aucune query.

### 6.4 Domaine 4 — Contacts
Modèle : relation (`PENDING`/`ACCEPTED`) **distincte** du blocage (deux tables : un blocage n'efface pas une relation ACCEPTED).
| Route | Détails |
|---|---|
| `GET /api/community/contacts` | Contacts ACCEPTED, enrichis du profil du pair. |
| `POST /api/community/contacts/request` | **Envoyer `{ "q": "<userId DSI ou @pseudo>" }`** (le contrat accepte aussi `userId`, `peerId`, `targetUserId`, `pseudonym` ; on n'utilise que `q`). Réponse : `{ created, duplicate, accepted, cross, alreadyContacts }` — **tous sont des succès**. `VALIDATION` = « Vous ne pouvez pas vous ajouter vous-même. » ; `MEMBER_NOT_IN_COMMUNITY`. |
| `GET /api/community/contacts/requests` | `{ incoming: [...], outgoing: [...] }`. |
| `POST /api/community/contacts/requests/{id}/accept` | Destinataire seulement (`FORBIDDEN` : propre demande ; `INVALID_STATE` : plus en attente). |
| `POST /api/community/contacts/requests/{id}/reject` | Destinataire seulement (`FORBIDDEN` : utiliser l'annulation). |
| `DELETE /api/community/contacts/requests/{id}` | Annulation, demandeur d'origine seulement (`FORBIDDEN`). |
| `DELETE /api/community/contacts/{peerRef}` | Supprime une relation ACCEPTED. `peerRef` = `userId` ou `@pseudo` (encodé URL). Erreurs : `NOT_FOUND`, `INVALID_STATE`. |
| `POST /api/community/contacts/block` | `{ "peerId": "" }` — **nom du champ [À CONFIRMER]** (non formalisé ; corriger si le Worker répond `VALIDATION`). |
| `POST /api/community/contacts/unblock` | `{ "peerId": "" }` — idem. |

Contact observé : `{ relationId, userId, displayName, pseudonym, avatarUrl, avatarThumbUrl, contactSince, status }`.

### 6.5 Domaine 5 — Appels audio (LiveKit)
V1 = **audio uniquement** (`mode:"video"` refusé). Quotas : 300 s/jour, 3000 s/mois, 150 s max par appel, sonnerie 30 s (**affichage uniquement**, le serveur décide).
| Route | Détails |
|---|---|
| `GET /api/community/calls` | 50 derniers (appelant ou appelé). |
| `POST /api/community/calls` | `{ "calleeId": "<userId DSI ou @pseudo>", "mode": "audio" }` — **le nom `calleeId` n'est pas formalisé par le contrat [À CONFIRMER]** (corriger si `VALIDATION`). Réponse `data` = appel, avec `joinConfiguration` (voir « Objet appel » ci-dessous). Erreurs : `CONTACT_REQUIRED`, `INTERACTION_UNAVAILABLE`, `CALL_QUOTA_EXCEEDED` (« Quota d'appel épuisé. »), `MEDIA_SERVICE_UNAVAILABLE`. |
| `POST /api/community/calls/{id}/join` | Confirme la présence, idempotent. Erreurs : `FORBIDDEN` (pas partie à l'appel), `INVALID_STATE` (« déjà terminé »), `NOT_FOUND`. |
| `POST /api/community/calls/{id}/decline` | |
| `POST /api/community/calls/{id}/cancel` | |
| `POST /api/community/calls/{id}/end` | `data: { status, actualDurationSeconds }` |
| `GET /api/community/calls/quota` | `data` = quota. |

Connexion audio côté Android : **SDK `livekit-android`**. Erreurs du runtime (codes internes à reprendre) : `SDK_MISSING`, `CONNECT_FAILED`, `MIC_DENIED`, `MIC_UNAVAILABLE`. Micro activé après connexion ; sur refus de permission → nettoyer (déconnecter) et renvoyer `MIC_DENIED`. Service de premier plan Android obligatoire pendant l'appel.
**Objet appel** (champs lus par le site) : `{ id` (ou `callId), status, direction: "incoming"|"outgoing", startedAt, peer, maximumDurationSeconds, joinConfiguration }`.
- `status` : `RINGING`, `ACCEPTED`, `CONNECTED`, `ENDED`, `DECLINED`, `CANCELLED`, `FAILED`, `NO_ANSWER` (liste complète côté Worker : **[À CONFIRMER]**). Statuts **finaux** (à ne pas rejoindre) : `ENDED`, `DECLINED`, `CANCELLED`, `FAILED`, `NO_ANSWER` — le site affiche « appel déjà terminé » si l'appel trouvé a l'un d'eux.
- **Appel entrant à afficher** : `direction === "incoming"` **et** `status === "RINGING"` **et** `startedAt` il y a moins de **40 s** (fenêtre d'affichage côté site ; la sonnerie réelle du Worker est de 30 s).
- `joinConfiguration` pour LiveKit : `{ wsUrl, token }` **ou** `{ wsUrl, tokens: { caller, callee } }` (choisir `token`, sinon `tokens[rôle]` ; rôle = `caller` si on appelle, `callee` si on reçoit) ; `maxDurationSeconds` (défaut 150) ; un repli `roomUrl` existe pour un fournisseur externe (ouvrir l'URL dans le navigateur).
- Délai de sonnerie côté appelant : **30 s** sans réponse → `cancel` + message « pas de réponse ».
- Quota (`GET /calls/quota`) : `dayRemainingSeconds`, `monthRemainingSeconds` (affichés en minutes).
- Ordre de l'écran d'appel : `join` → connexion LiveKit → micro. Fin : `end` (ou `cancel` si l'appelant raccroche avant réponse).

### 6.6 Domaine 6 — Notifications & réglages
| Route | Détails |
|---|---|
| `GET /api/community/notifications` | 50 dernières, payload désérialisé. Champs : `{ id, type, read, payload, fromPseudonym, fromDisplayName, createdAt }`. **Types connus** : `message.created` (`payload.conversationId` → ouvre la conversation), `support.message` (→ fil Support), `call.incoming` (→ liste des appels). Tout autre type : affichage générique, pas de navigation. |
| `POST /api/community/notifications/{id}/read` | **Idempotent silencieux** : si l'id n'est pas à l'appelant, répond quand même `{ id, read:true }` — ne **pas** y voir une preuve d'appartenance. |
| `GET /api/community/settings` | `{ privacy: { showLastSeen, allowDirectMessages, allowCalls }, prefs: { notifyMessages, notifyCalls, notifySpaces } }` — objets **vides** si jamais configuré. |
| `PUT /api/community/settings` | **Remplace** les deux blocs. N'envoyer **que des booléens** sur ces 6 clés ; toute autre clé ou valeur non booléenne est **ignorée silencieusement**. |

### 6.7 Domaine 7 — Profil & annuaire
| Route | Détails |
|---|---|
| `GET /api/community/me` | **Toujours 200.** Soit un profil, soit `{ profile:null, communityStatus:"INCOMPLETE", needsProfile:true, userId }` (signal d'onboarding, jamais une erreur). Garde allégée (répond sans profil). |
| `POST /api/community/me/profile` | Onboarding. `{ "displayName": "≤80", "pseudonym"?: "", "discoverable"?: bool }`. **Idempotent** : profil existant renvoyé avec `existing:true`. Erreurs : `VALIDATION` (+`field`), `CONFLICT` (pseudonyme pris, `field:"pseudonym"`). |
| `PATCH /api/community/me/profile` | `displayName` et/ou `discoverable`. Ne régénère jamais le pseudonyme. Erreurs : `VALIDATION`, `PROFILE_INCOMPLETE`. |
| `PATCH /api/community/me/pseudonym` | `{ "pseudonym": "" }`. Erreurs : `VALIDATION` / `PSEUDONYM_INVALID` (trop court/invalide), `CONFLICT` / `PSEUDONYM_TAKEN` (déjà pris). |
| `GET /api/community/members/search?q=` | **Uniquement** par `userId` DSI exact (préfixe `DSI-`) ou pseudonyme normalisé. **Jamais** par nom d'affichage. Ne renvoie que `discoverable:true` et statut `ACTIVE`. |
| `GET /api/community/members/{id}` | `id` = `userId` ou `@pseudo` (encodé). `data: { profile, self, contact, blocked }`. 404 volontairement neutre (« Introuvable dans la communauté. »). |
| `POST /api/community/me/avatar` | **Deux usages sur la même route** (voir §10) : JSON `{ "fileId" }` (première pose ; `VALIDATION` si un avatar existe → utiliser `replace`) **ou** multipart avec champs `file` (512 px) et `thumb` (96 px). |
| `POST /api/community/me/avatar/replace` | `{ "fileId" }` → `data.previousFileId`. |
| `DELETE /api/community/me/avatar` | Idempotent (`alreadyNull`). **La version effective du site** renvoie le profil (champs photo à `null`). Erreurs `STORAGE_UNAVAILABLE` / `STORAGE_DELETE_FAILED` (« L'avatar reste en place »). |

Profil observé : `{ userId, displayName, pseudonym, publicPseudonym, discoverable, communityStatus, avatarUrl, avatarThumbUrl, avatarFileId }`.
Les routes de profil répondent `{ profile, … }` : **toujours déballer** (`data.profile ?? data`).
Pseudonyme réservé (refusé par le Worker) : voir §7.

### 6.8 Domaine 8 — Fichiers joints
`POST /api/community/media/{fileId}/read-url` — Corps vide. URL de lecture temporaire (**TTL 300 s**). Accès : auteur du message **ou** membre de la conversation, sinon `ACCESS_DENIED`. `fileId` = `attachmentFileId` du message (jamais reconstruit).

### 6.9 Domaine 9 — Support DSI
Fil **séparé** (identité système `DSI_SUPPORT`, jamais dans `/conversations`).
| Route | Détails |
|---|---|
| `GET /api/community/support` | Récupère **ou crée** (idempotent) la conversation Support ; 201 si création, 200 sinon. `data` inclut `created`. |
| `GET /api/community/support/messages?limit=&before=` | Plafond 100. |
| `POST /api/community/support/messages` | `{ "messageId": "<uuid>", "body"?: "", "attachmentFileId"?: "" }` — **le champ texte s'appelle `body` ici (pas `text`)**. `data: { duplicate, ... }` (`duplicate:true` = retry sans doublon). `VALIDATION` = message vide. |

---

## 7. Règles de validation côté client (confort UX uniquement)

À reprendre dans `core:common` ; **le serveur reste l'autorité** (afficher la valeur renvoyée après enregistrement).

- Nom d'affichage : **≤ 80** caractères.
- Pseudonyme : **2 à 32** caractères après normalisation. Normalisation : retirer espaces de bordure et `@` initiaux → minuscules → supprimer les accents (NFD, retirer U+0300–U+036F) → tout caractère hors `a-z0-9_` devient `_` → fusionner les `_` répétés → retirer les `_` de bordure → tronquer à 32.
- Pseudonymes **réservés** (refusés, après normalisation) : `support, admin, administrateur, dsi, dsiacademy, dsi_academy, moderateur, officiel, equipe, staff, system, systeme, me, api, root, null, undefined`.
- Initiales : 2 lettres maximum à partir du nom affiché ; `?` si vide.
- Avatar : types `image/jpeg`, `image/png`, `image/webp` ; **2 Mo max** ; image **512 px** + miniature **96 px** (sortie WebP).
- Candidature vendeur : fichiers **8 Mo max** par fichier.
- Message communauté : texte **≤ 4000** ; `attachmentFileId` **≤ 80**. Espace : nom **≤ 80**, description **≤ 500**.

---

## 8. Données locales (jamais envoyées à un Worker)

Sur le site : `localStorage`. Sur Android : **Room** (listes) ou **DataStore** (clé-valeur).

| Clé web | Contenu | Cible Android |
|---|---|---|
| `dsi_session_token`, `dsi_session_state` | session (voir §1.2) | `SessionStore` chiffré (DataStore + Keystore) — persistant si « se souvenir », sinon mémoire |
| `dsi_inscription_draft` | `{ userId, paymentTrackingToken, paymentUrl, etape }` | DataStore (supprimé à la fin de l'inscription) |
| `dsi_progress` | `{ [clé]: { position, duration, percent, completed, extra, updatedAt } }`, **200 entrées max** (purge des plus anciennes) | Room. Clé libre ex. `videos:12`, `formations:module:7`. Vidéo/audio : position/durée en **secondes** ; PDF : position = page courante, durée = nb de pages ; texte : position = **pourcentage** de défilement. `extra` : marque-pages, vitesse… |
| `dsi_continue` | entrées « Continuer » (page Academy) | Room |
| `dsi_selections` | sélections de l'utilisateur (listes) | Room |
| `dsi_favorites` | favoris locaux des maisons de contenu (distincts des favoris Boutique/Marketplace serveur) | Room |
| `dsi_preferences` | préférences (thème, langue, lecture) | DataStore |
| `dsi_notifications_local` | `{ id, title, body, type, read, createdAt }`, **50 max** | Room |
| `dsi_mp_checkout_<listingId>` | intention d'achat Marketplace (§1.7) | mémoire de session |

Notifications globales : `GET {ADMIN}/public/notifications` (public, sans auth) → `{ notifications: [ { id, titre, message, type, date_debut_affichage, date_fin_affichage } ] }`. Si l'appel échoue ou renvoie une liste vide → **boîte locale de secours** (état « local »). Ne pas faire échouer l'écran.

---

## 9. Inventaire (pour la vérification automatique)

| Worker | Routes |
|---|---|
| Paiement | 15 (dont 1 non utilisée en v1 : 2.13) |
| Contenu | catalogue, catégories, recherche, fiche par id, fiche par slug, assets/best, tree, deliver = **8** |
| Boutique | 16 |
| Marketplace | 33 |
| Communauté | conversations 4, espaces 5, activité 1, contacts 9, appels 7, notifications/réglages 4, profil/annuaire 9, fichiers 1, support 3 = **43** |
| Admin | 1 |
| **Total** | **116** |

Décompte vérifié par script sur le code du site : Paiement 15 appels, Boutique 16, Marketplace 33, Communauté 45 appels = **43 routes distinctes** (le site contient 2 doublons : `POST /me/avatar` et `DELETE /me/avatar`, voir §10). Contenu : 8 routes génériques, partagées par les 4 maisons (le site les répète par type : 4 services × catalogue/catégories/recherche/fiche).

Vérification : chaque route ci-dessus doit avoir **une** fonction dans une interface Retrofit et **un** test de contrat (réponse JSON d'exemple → DTO → modèle de domaine).

---

## 10. Pièges et points à vérifier (à lire avant de coder)

1. **`ApiClient` du site (Paiement)** étale `...options` après `headers` dans `fetch` : passer des `headers` personnalisés **remplace** les en-têtes calculés (dont `Authorization`). Aucun appel actuel ne combine les deux ; dans l'app, **les en-têtes se cumulent** via intercepteurs : ne pas reproduire ce comportement.
2. **Deux `deleteAvatar` dans `profile.service.js`** : le second écrase le premier (le site utilise celui qui renvoie le **profil** ; le premier renvoyait `alreadyNull`). À trancher avec le Worker : renvoyer le profil ET gérer `alreadyNull`.
3. **`/me/avatar` a deux usages** (JSON `{fileId}` et multipart `file`+`thumb`). Le site utilise en pratique le **multipart** (`uploadAvatar`). Utiliser une seule méthode Retrofit multipart ; `setAvatar(fileId)` est un reliquat **[À CONFIRMER]**.
4. **Champs non formalisés** (`calleeId`, `peerId` pour block/unblock) : implémenter tels quels, **journaliser le `code` en cas de `VALIDATION`** et remonter à l'architecte.
5. **`text` vs `body`** : message de conversation (`text`) ≠ message Support (`body`). Ne pas unifier à l'envoi.
6. **`first_name`/`last_name` vs `prenom`/`nom`** dans le profil Paiement : accepter les deux (`@SerialName` + alternative dans le mapper).
7. **Écrans qui utilisent des données de démonstration** (`mockCatalog`, `mockMarketplace`, `mockCommunity`, `mockChats`, `mockProducts`) : ce sont des **maquettes, pas des contrats**. L'app Android ne les embarque pas ; elle affiche des **états vides/chargement** quand le Worker ne répond pas.
8. **Tous les statuts, codes et noms de champ** ci-dessus viennent du **code du site**, pas d'une documentation officielle des Workers. Avant la première mise en production d'un domaine : relire le contrat du Worker correspondant (`worker_admin_complet.zip`, `dsi-worker-community.zip`…) pour confirmer les `[À CONFIRMER]`.
9. **Réponse de lecture Boutique (`read-url`)** : forme inconnue, même pour le site (voir 4.16). Premier test réel = journaliser la réponse brute.
10. **Route non utilisée** : `GET /api/membre/dsi/historique` (2.13). L'écran « Mon DSI » dépend de `solde` + `retraits`, pas de cette route.
11. **Aucune pagination par curseur côté écran** : le défilement infini du Contenu est une **nouveauté** de l'app, à valider avec le Worker avant de la promettre.

---

## 11. Journal des révisions

- **v1.0** : extraction des 40 services.
- **v1.1** : points levés par lecture des écrans — statuts de paiement d'inscription, alias du profil, statuts de retrait, forme des moyens de retrait (chaînes), flux d'achat Marketplace (codes, intention d'achat, livraison, reconcile), candidature vendeur (7 champs), création d'annonce, tableau de bord/gains/analytics vendeur, objet appel et `joinConfiguration`, types de notifications. Restent **[À CONFIRMER]** : champs détaillés de produit/contenu/abonnement, `read-url` Boutique, forme du curseur, `portfolio` et `settings` vendeur, `calleeId` / `peerId`.

---

## 12. Squelette Kotlin attendu (pour chaque domaine)

```kotlin
// :domain — modèle propre, aucun Android, aucun Retrofit
data class Balance(val identifiantDsi: String?, val soldeDsiActuel: Double, val totalDsiGagnes: Double, val dateMiseAJour: String?)

// :domain — résultat unique pour tout le projet (remplace { success, message, sessionExpired })
sealed interface DsiResult<out T> {
    data class Success<T>(val data: T) : DsiResult<T>
    data class Failure(val error: AppError) : DsiResult<Nothing>
}

// :data:<domaine> — une interface Retrofit par domaine, une fonction par route
interface PaymentApi {
    @GET("api/membre/solde")  suspend fun getBalance(): BalanceDto
}

// :data:<domaine> — 3 gabarits de réponse (A, B, C) → 3 façons de désérialiser
//   A  : DTO à la racine (+ champ optionnel `erreur`)
//   B  : Envelope<T>(success, error, code, field, data)
//   C  : DTO à la racine, erreur = { "error": "CODE" }
@Serializable data class Envelope<T>(val success: Boolean = false, val error: String? = null,
    val code: String? = null, val field: String? = null, val data: T? = null)
```

Règles de mapping : DTO tolérants (`ignoreUnknownKeys = true`, valeurs par défaut partout, `JsonElement` pour les champs « chaîne ou nombre »), conversion vers le domaine **dans le mapper uniquement**, `safeCall { }` unique qui traduit exceptions et enveloppes en `DsiResult`.
