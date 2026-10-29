# ARCHITECTURE.md — DSI Academy Android (Kotlin)

> Règles d'architecture OBLIGATOIRES. À relire avant CHAQUE tâche.
> Rédigé par l'architecte en chef. Le codeur exécute, il ne change jamais ces règles.
> Documents liés : `CONTRATS-API.md` (routes et formats), `DESIGN-TOKENS.md` (visuel), `docs/TACHES/` (tâches).
> Version 2.0 — passage à un module Gradle unique `app` organisé en paquets. Remplace la version 1.1 (multi-modules).

---

## 0. Les 7 règles d'or

1. **Un écran ne connaît que son ViewModel.** Jamais un Repository, une API, un DTO, Room ou Retrofit.
2. **Un ViewModel ne connaît que des interfaces** du paquet `domain`. Jamais `data`.
3. **Tout le cache vit dans les Repository** (paquet `data`). Jamais dans un écran ni un ViewModel.
4. **`domain` et `core/common` sont du Kotlin pur** : aucun import `android.*`, `androidx.*`, Retrofit, OkHttp ou JSON. `domain` ne dépend que de `core.common`.
5. **Aucune valeur visuelle en dur** : couleurs, polices, rayons, espacements viennent du thème. Aucun texte en dur : tout vient de `strings.xml`.
6. **Aucune route, aucun champ inventés.** Seul `CONTRATS-API.md` fait foi. Un point marqué [À CONFIRMER] = on s'arrête et on pose la question.
7. **Une tâche = les fichiers listés dans sa fiche, et rien d'autre.**

---

## 1. Langages et outils

| Usage | Choix |
|---|---|
| Langage | **Kotlin** (dernière version stable), coroutines + Flow |
| Interface | **Jetpack Compose** + Material 3 entièrement personnalisé. Pas de XML pour les écrans |
| Build | **Gradle Kotlin DSL** (`.kts`) + `gradle/libs.versions.toml` (versions centralisées) |
| Injection | **Hilt** |
| Navigation | **Navigation Compose**, routes typées |
| Réseau | **Retrofit + OkHttp + kotlinx.serialization** |
| Local | **Room** (listes), **DataStore** (clé-valeur), Keystore (chiffrement de la session) |
| Médias | **Media3 (ExoPlayer)** audio/vidéo ; lecteur PDF à trancher |
| Images | **Coil** |
| Tâches de fond | **WorkManager** (téléchargements) |
| Notifications | **Firebase Cloud Messaging** |
| Appels | **livekit-android** |
| Tests | JUnit, MockK, Turbine (Flow), tests de contrat JSON |
| Qualité | ktlint + detekt |
| XML | uniquement : manifeste, `strings.xml` (FR/EN), thèmes système, icônes |

- Identifiant d'application (`applicationId`) : **`app.dsiacademy.academy`**. Modifiable tant que l'app n'est pas publiée ; après publication, il ne change plus.
- Paquet racine : `app.dsiacademy.academy`.
- `minSdk` : **24** (proposé, public mobile d'entrée de gamme ; à valider). `targetSdk` : la valeur exigée par Google Play au moment de la publication.
- **Interdit d'ajouter une dépendance sans accord de l'architecte** (une ligne dans `libs.versions.toml` = une décision).

---

## 2. Module unique et règles de dépendance

Il n'existe **qu'un seul module Gradle : `:app`**. Les frontières entre couches sont des **paquets**, pas des modules. Le compilateur ne les fait donc plus respecter : c'est le workflow de vérification (section 14) qui le fait.

```
feature.*   → domain.*, core.*              (JAMAIS data, JAMAIS une autre feature)
data.*      → domain.*, core.*
domain.*    → core.common uniquement        (Kotlin pur)
core.common → rien                          (Kotlin pur)
core.*      → autres core.* uniquement      (jamais domain / data / feature)
navigation, di → tout (c'est le câblage de l'application)
```

- Seul le paquet `di` lie chaque interface de `domain` à son implémentation de `data` (Hilt).
- Les `feature` ne se connaissent pas entre elles : la navigation inter-features passe par des routes déclarées dans `navigation`.
- **Ne jamais ajouter un import interdit pour « faire passer » un build.** C'est le signe qu'on viole l'architecture.

---

## 3. Arborescence

```
DSI-Academy-app-Android/
├── settings.gradle.kts                  # include(":app")
├── build.gradle.kts
├── gradle/libs.versions.toml
├── docs/                                # ce document, CONTRATS-API.md, DESIGN-TOKENS.md, TACHES/
├── .github/workflows/
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/
        │   │   ├── values/strings.xml       # FR (défaut)
        │   │   ├── values-en/strings.xml    # EN
        │   │   └── font/                    # Fraunces, Manrope, IBM Plex Mono
        │   └── kotlin/app/dsiacademy/academy/
        │       ├── DsiApplication.kt
        │       ├── MainActivity.kt
        │       ├── navigation/              # NavHost racine, RequireAuth, routes communes
        │       ├── di/                      # modules Hilt (liaisons domain → data)
        │       ├── core/
        │       │   ├── common/              # Kotlin PUR : DsiResult, AppError, formats, validations, MemoryCache
        │       │   ├── designsystem/
        │       │   │   ├── theme/           # DsiColors, DsiTypography, DsiShapes, DsiSpacing
        │       │   │   ├── components/      # composants réutilisés par 2 écrans ou plus
        │       │   │   └── icons/
        │       │   ├── network/             # OkHttp, Retrofit, intercepteurs, WorkerUrls, RateLimitGate, safeCall
        │       │   ├── session/             # SessionStore chiffré
        │       │   ├── database/            # Room : AppDatabase, migrations
        │       │   └── media/               # service Media3
        │       ├── domain/                  # Kotlin PUR, par domaine métier
        │       │   ├── member/
        │       │   ├── payment/
        │       │   ├── content/
        │       │   ├── shop/
        │       │   ├── marketplace/
        │       │   └── community/
        │       ├── data/                    # implémentations, par domaine métier
        │       │   ├── member/
        │       │   ├── payment/
        │       │   ├── content/
        │       │   ├── shop/
        │       │   ├── marketplace/
        │       │   └── community/
        │       │       # chaque domaine : api/  dto/  mapper/  local/  XxxRepositoryImpl.kt
        │       └── feature/                 # écrans, par fonctionnalité
        │           ├── public/
        │           ├── member/
        │           │   ├── MemberNavGraph.kt
        │           │   ├── login/
        │           │   ├── profile/
        │           │   │   └── components/
        │           │   └── settings/
        │           ├── academy/
        │           ├── content/
        │           ├── shop/
        │           ├── marketplace/
        │           ├── community/
        │           └── assistant/
        ├── test/kotlin/app/dsiacademy/academy/     # tests unitaires, même arborescence que main
        └── androidTest/kotlin/app/dsiacademy/academy/  # tests d'interface (si nécessaires)
```

### Règles de rangement
- Chemin d'un écran : `feature/<fonctionnalité>/<sous-page>/<Ecran>.kt`.
- Au plus **2 niveaux** sous `feature/<fonctionnalité>/`. Au-delà, on crée une nouvelle fonctionnalité ou on remonte la sous-page.
- Un sous-dossier n'est créé que s'il contient **au moins 2 fichiers**. Un fichier isolé reste à côté des autres.
- Le repository est dans `domain/<domaine>/` (interface) et `data/<domaine>/` (implémentation), **jamais dans `feature/`**.
- Un domaine est un paquet, pas un module : les règles de dépendance de la section 2 se respectent à la main et se vérifient à l'étape 1 de la section 14.
- Dans un paquet de `feature`, ui, viewmodel et état sont **dans le même dossier** (pas de sous-dossiers `ui/`, `viewmodel/`, `state/`).
- Les tests suivent exactement le même chemin dans `src/test/` (ou `src/androidTest/`).

---

## 4. Responsabilités par couche

### 4.1 Écrans Compose (`feature/…`)
- Fonctions `@Composable` sans logique métier. Reçoivent un `UiState` et des lambdas (`onAction`).
- Deux niveaux : `XxxScreen` (branché au ViewModel via Hilt) et `XxxContent(state, onAction)` (pur, prévisualisable avec `@Preview`).
- Interdit : `launch`, appel réseau, accès disque, formatage de montant ou de date maison (utiliser `core.common`).

### 4.2 ViewModel et état (`feature/…`)
- Un ViewModel par écran. Expose **un seul** `StateFlow<XxxUiState>` et reçoit des `XxxUiAction`.
- Les événements ponctuels (navigation, toast) passent par un `Channel`/`SharedFlow` d'`XxxUiEvent`.
- Appelle `UseCase` ou `Repository` (interfaces du paquet `domain`). Transforme `DsiResult` en `UiState`.
- Interdit : `Context`, `Retrofit`, `Room`, importer un DTO ou un paquet `data`.

```kotlin
// feature/member/profile/BalanceUiState.kt
sealed interface BalanceUiState {
    data object Loading : BalanceUiState
    data class Success(val balance: Balance) : BalanceUiState
    data class Error(val error: AppError) : BalanceUiState
}
sealed interface BalanceUiAction { data object Retry : BalanceUiAction }
```

### 4.3 `domain` (Kotlin pur)
- Paquets `domain.<domaine>`. Aucun import Android, Retrofit, OkHttp, JSON ou Room.
- `model/` : `data class` immuables, noms métier (`Balance`, `Subscription`), jamais le nom d'un DTO.
- `repository/` : interfaces. Retournent `DsiResult<T>` ou `Flow<DsiResult<T>>`.
- `usecase/` : uniquement pour combiner plusieurs sources ou porter une règle. **Pas de UseCase qui recopie un Repository.** Un seul verbe : `operator fun invoke`.

### 4.4 `data` (implémentations)
- `api/` : interface Retrofit, **une fonction par route** de `CONTRATS-API.md`.
- `dto/` : `@Serializable`, tolérants : valeurs par défaut partout, `JsonElement` pour les champs « chaîne ou nombre », `ignoreUnknownKeys`.
- `mapper/` : DTO → modèle de domaine. **Toute normalisation** (nombres, dates, alias de champs) est faite ici, une seule fois.
- `local/` : DAO et entités Room de ce domaine.
- `XxxRepositoryImpl` : orchestre api + local + cache, traduit les erreurs avec `safeCall`.

### 4.5 `core`
Briques techniques sans logique métier. `core.designsystem` ne connaît aucun modèle métier. `core.common` est du Kotlin pur (aucun `android.*`, aucun `androidx.*`).

---

## 5. Résultats et erreurs (un seul type pour tout le projet)

```kotlin
// core/common — Kotlin pur
sealed interface DsiResult<out T> {
    data class Success<T>(val data: T) : DsiResult<T>
    data class Failure(val error: AppError) : DsiResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object InvalidResponse : AppError
    data object SessionExpired : AppError
    data object NotLoggedIn : AppError
    data object SubscriptionInactive : AppError
    data object AccountDenied : AppError
    data object CommunityProfileRequired : AppError
    data class RateLimited(val retryAfterSeconds: Int?) : AppError
    data object NotFound : AppError
    data class Validation(val field: String?) : AppError
    data object Forbidden : AppError
    data object Conflict : AppError
    data object ServiceUnavailable : AppError
    data class Unknown(val code: String?, val message: String?) : AppError
}
```

Règles :
- La table code d'erreur → `AppError` est dans `CONTRATS-API.md` §1.6. Elle est codée **une seule fois** dans `core.network` (`safeCall`).
- L'expiration de session est gérée **uniquement** par `SessionExpiredInterceptor` (purge + événement global → écran de connexion). **Aucun écran ni Repository ne teste un 401.**
- Les écrans traduisent `AppError` en texte via `strings.xml`. Jamais de message brut du serveur ni d'exception affichés.

---

## 6. Repository et cache

Politique de cache **choisie endpoint par endpoint**, dans le Repository, en une ligne lisible :

| Niveau | Quand | Outil |
|---|---|---|
| Aucun | paiement, retrait, achat, renouvellement | — |
| Mémoire + durée de vie | solde, profil, abonnement (ex. 30 s) | `MemoryCache` (`core.common`) |
| Disque | catalogues, conversations, progression, favoris | Room (`core.database`) |

**Jamais en cache disque** : session, `accessProof`, `paymentTrackingToken`, URLs temporaires (`playback_url`, `read_url`, `read-url`).
Les listes affichées sont exposées en `Flow` pour que l'écran se mette à jour seul.

```kotlin
// data/payment/PaymentRepositoryImpl.kt
class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApi,
    private val cache: MemoryCache,
) : PaymentRepository {
    override suspend fun refreshBalance(): DsiResult<Balance> {
        cache.get<Balance>("balance", maxAge = 30.seconds)?.let { return DsiResult.Success(it) }
        return safeCall { api.getBalance().toDomain() }
            .also { if (it is DsiResult.Success) cache.put("balance", it.data) }
    }
}
```

---

## 7. Réseau

- **6 URLs de base** dans `WorkerUrls` (`core.network`), configurables par build (`BuildConfig`). **Jamais d'URL `workers.dev` en dur ailleurs.**
- **Trois mécanismes d'authentification, jamais mélangés** : `Bearer` (Paiement), `X-Access-Proof` (4 autres Workers), `X-Payment-Tracking-Token` (1 route). Implémentés par intercepteurs OkHttp pilotés par une annotation sur la fonction Retrofit (`@Auth`).
- **Trois gabarits de réponse** (A Paiement, B enveloppe `{success,error,code,data}`, C Contenu) : décrits dans `CONTRATS-API.md` §1.3, désérialisés dans `data`.
- Rate limit : `RateLimitGate` dans `core.network`. Les sondages se mettent en pause pendant `retryAfterSeconds`.
- Fichiers : multipart, jamais de `Content-Type` posé à la main.

---

## 8. Session et sécurité

- `SessionStore` (`core.session`) : jeton + état + `accessProof` **chiffrés** (DataStore + Keystore). Persistant si « se souvenir de moi », sinon mémoire.
- `accessProof` et `paymentTrackingToken` : chaînes opaques, jamais décodées ni journalisées.
- **Aucun secret, clé API ou identifiant de feuille dans l'application.** Aucun fichier `.jks`, `.keystore`, `keystore.properties` ou `local.properties` dans le dépôt.
- Journaux : jamais de jeton, mot de passe, numéro de paiement ni `accessProof`.
- `allowBackup="false"` ; pas de capture d'écran sur les écrans de lecture de contenu protégé (`FLAG_SECURE`), à confirmer avec l'architecte.

---

## 9. Design system (même visuel que le site)

- Tokens dans `core/designsystem/theme/`, copiés de `global.css` (voir `DESIGN-TOKENS.md`) : `DsiColors`, `DsiTypography`, `DsiShapes`, `DsiSpacing`, ombres, verre dépoli.
- Thèmes **clair et sombre**.
- Polices : **Fraunces** (titres, grands chiffres, jamais < 14 sp, jamais sur boutons/formulaires/tableaux), **Manrope** (corps, boutons, formulaires), **IBM Plex Mono** (petits libellés).
- Accent doré `#D4AF37` utilisé avec parcimonie (≈ 5 %). Rayons 6 / 10 / 16 dp.
- Tout composant réutilisé dans 2 écrans ou plus va dans `core/designsystem/components/`.
- **Interdit dans `feature`** (contrôlé automatiquement) : `Color(0x…)`, `Color.Red`/`Color.White`/etc., `fontSize = 14.sp`, `12.dp` littéral, `Text("…")`, `contentDescription = "…"`. Toujours via le thème et `stringResource`.
- Dérogation exceptionnelle : commentaire `// dsi-allow-literal` sur la ligne, **avec justification**, soumis à la revue de l'architecte. Ne jamais l'utiliser sans l'avoir signalé.
- Chaque `XxxContent` a au moins un `@Preview` clair et un sombre.
- Les écrans sont d'abord **fidèles au site** ; toute amélioration se discute avec l'architecte.

---

## 10. Textes et langues

- `res/values/strings.xml` (FR, défaut) et `res/values-en/strings.xml` (EN). Clés : `<feature>_<ecran>_<élément>` (ex. `member_balance_title`).
- Les messages d'erreur FR du site (cités dans `CONTRATS-API.md`) sont repris tels quels.
- Dates, montants, pluriels : via `core.common` (formateurs uniques).

---

## 11. Navigation

- Graphe racine dans `navigation/`. Chaque `feature/<fonctionnalité>/` expose **son** graphe (`XxxNavGraph.kt`) et ses routes typées.
- Garde d'accès (`RequireAuth`) : si pas de session → écran de connexion. `SessionEvent.Expired` renvoie vers la connexion et vide la pile.

---

## 12. Données locales

Voir `CONTRATS-API.md` §8 (équivalences `localStorage` → Room/DataStore). Progression : 200 entrées max, purge des plus anciennes. Notifications locales : 50 max.

---

## 13. Médias

- Audio/vidéo : Media3, **service de lecture au premier plan** (lecture en arrière-plan, notification média), dans `core/media/`.
- Livraison de contenu : voir `CONTRATS-API.md` §3.7 (4 formes). `playback_url` / `read_url` donnés **tels quels** au lecteur, jamais téléchargés en mémoire.
- Téléchargement : `WorkManager`, écriture sur disque, jamais de chargement complet en mémoire.
- Appels : service de premier plan pendant l'appel, permission micro demandée au moment de l'appel.

---

## 14. Tests et contrôles automatiques (workflow GitHub Actions « Vérifications DSI Android »)

Le workflow se lance **à la main** et par **étapes cumulatives** :

| Étape | Contenu | Nécessite Gradle |
|---|---|---|
| 1 · Contrôles rapides | présence des docs ; règles d'architecture ; contenu interdit ; secrets | non |
| 2 · Qualité | + ktlint + detekt | oui |
| 3 · Compilation | + compilation de `:app` (sans APK) | oui |
| 4 · Tests | + tests unitaires et tests de contrat | oui |
| 5 · APK debug | + construction de l'APK debug | oui |

Règles contrôlées à l'étape 1 (un seul manquement = échec) :
1. `feature` n'importe pas `data`, ni DTO/API/mapper/Room/Retrofit/OkHttp, ni une autre `feature`.
2. `domain` : aucun import Android/Retrofit/OkHttp/kotlinx.serialization/Room ; ne référence que `core.common`.
3. `core` n'importe pas `domain`/`data`/`feature` ; `core.common` sans `android.*`/`androidx.*`.
4. Aucun `401` dans `data` ni `feature` (géré par l'intercepteur central).
5. Aucune URL `workers.dev` hors de `core.network`.
6. Aucun `GlobalScope` ni `runBlocking` dans le code principal.
7. Aucune valeur visuelle ni texte en dur dans `feature` (voir §9).
8. Aucun secret ni fichier de clés dans le dépôt.
9. Aucun fichier `README.md` placeholder ; aucun dossier vide versionné.

Un ViewModel sans test = tâche non terminée. Les tests de contrat : un JSON d'exemple par route → DTO → modèle (extrait de `CONTRATS-API.md`).

---

## 15. Conventions de nommage

| Élément | Convention | Exemple |
|---|---|---|
| Écran | `XxxScreen` / `XxxContent` | `BalanceScreen` |
| ViewModel | `XxxViewModel` | `BalanceViewModel` |
| État / action / événement | `XxxUiState` / `XxxUiAction` / `XxxUiEvent` | `BalanceUiState` |
| Repository | interface `XxxRepository` (domain), impl `XxxRepositoryImpl` (data) | `PaymentRepository` |
| API Retrofit | `XxxApi` | `PaymentApi` |
| DTO / mapper | `XxxDto` / `XxxDto.toDomain()` | `BalanceDto` |
| UseCase | verbe + nom + `UseCase` | `LoadMemberDashboardUseCase` |
| Chaînes | `feature_ecran_element` | `member_balance_title` |
| Paquets | minuscules, sans tiret ni majuscule | `feature.member.profile` |

Un fichier = une classe principale. Fichiers de moins de ~300 lignes ; au-delà, découper.
Commentaires métier en **français**, identifiants en **anglais** (sauf termes du contrat : `accessProof`, `idRetrait`…).

---

## 16. Interdits absolus (motif de rejet immédiat)

- Appel réseau, Room ou `Context` depuis un écran ou un ViewModel.
- Import d'un paquet `data` depuis `feature`, ou d'une feature depuis une autre.
- Route, champ, code d'erreur ou statut absent de `CONTRATS-API.md`.
- Cache (mémoire/disque) dans un ViewModel ou un écran.
- Couleur, taille de police, espacement ou texte en dur dans `feature` ; usage de `// dsi-allow-literal` sans justification ni signalement.
- `GlobalScope`, `runBlocking` hors tests, `!!` non justifié.
- Test d'un 401 hors de l'intercepteur central.
- Journaliser un jeton, un mot de passe, un `accessProof`.
- Committer un secret, un fichier de clés ou `local.properties`.
- Modifier un fichier non listé dans la fiche de tâche.
- Ajouter une dépendance Gradle sans accord.
- Créer un dossier ou un fichier `README.md` « pour réserver la place ».
- Rendre des morceaux de code : **toujours des fichiers complets**.

---

## 17. En cas de doute

- Un point du contrat est [À CONFIRMER], ambigu ou contradictoire → **s'arrêter et poser la question**. Ne jamais combler un trou par une invention.
- Une règle de ce document semble empêcher la tâche → **ne pas la contourner** ; signaler le conflit.
- Besoin d'un fichier non listé → le demander, ne pas le créer.

---

## 18. Définition de « terminé » (checklist de chaque tâche)

- [ ] Les fichiers de la fiche existent, complets, sans autre fichier modifié.
- [ ] Le workflow GitHub Actions est vert (à l'étape demandée par la fiche).
- [ ] États couverts : chargement, succès, erreur, session expirée, vide.
- [ ] Aucune valeur visuelle ni texte en dur.
- [ ] `@Preview` clair + sombre.
- [ ] Tests écrits et verts (ViewModel / mapper / contrat).
- [ ] Aucune règle de la section 16 violée.
- [ ] Compte rendu court : fichiers créés, points [À CONFIRMER] rencontrés, questions.