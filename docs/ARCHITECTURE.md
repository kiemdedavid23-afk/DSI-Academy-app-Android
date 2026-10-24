# ARCHITECTURE.md — DSI Academy Android (Kotlin)

> Règles d'architecture OBLIGATOIRES. À relire avant CHAQUE tâche.
> Rédigé par l'architecte en chef. Le codeur (Grok) exécute, il ne change jamais ces règles.
> Documents liés : `CONTRATS-API.md` (routes et formats), `DESIGN-TOKENS.md` (visuel), `docs/TACHES/` (tâches).

---

## 0. Les 7 règles d'or

1. **Un écran ne connaît que son ViewModel.** Jamais un Repository, une API, un DTO, Room ou Retrofit.
2. **Un ViewModel ne connaît que des interfaces** de `:domain` (Repository, UseCase). Jamais `:data`.
3. **Tout le cache vit dans les Repository** (`:data:*`). Jamais dans un écran ni un ViewModel.
4. **Les modèles de `:domain` sont purs** : Kotlin seul, aucun Android, aucun JSON.
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

Paquet racine : `bf.dsi.academy`. `minSdk` : **24** (proposé, public mobile d'entrée de gamme ; à valider). `targetSdk` : la valeur exigée par Google Play au moment de la publication.
**Interdit d'ajouter une dépendance sans accord de l'architecte** (une ligne dans `libs.versions.toml` = une décision).

---

## 2. Modules et règle de dépendance

```
app      → feature:*, data:*, domain, core:*
feature  → domain, core:*            (JAMAIS data)
data     → domain, core:*
domain   → rien                       (Kotlin pur)
core     → autres core uniquement, jamais domain/data/feature
```

Le compilateur fait respecter cette règle : un `import` illégal ne compile pas. **Ne jamais ajouter une dépendance de module pour « faire passer » un import** : c'est le signe qu'on viole l'architecture.

Seul `:app` assemble tout (Hilt lie chaque interface de `:domain` à son implémentation de `:data:*`).

---

## 3. Arborescence

```
dsi-android/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── build-logic/                         # plugins Gradle communs
├── app/                                 # MainActivity, DsiApplication, di/, navigation/
├── core/
│   ├── designsystem/                    # theme/, components/, icons/
│   ├── network/                         # OkHttp, Retrofit, interceptors/, WorkerUrls
│   ├── session/                         # SessionStore chiffré
│   ├── database/                        # Room : progression, favoris, téléchargements…
│   ├── media/                           # service Media3
│   ├── common/                          # DsiResult, AppError, formats, validations
│   └── testing/
├── domain/
│   ├── model/                           # entités métier
│   ├── repository/                      # interfaces
│   └── usecase/                         # seulement s'il y a de la logique
├── data/
│   ├── payment/  content/  shop/  marketplace/  community/
│   │   # chacun : api/  dto/  mapper/  local/  XxxRepositoryImpl.kt
├── feature/
│   ├── public/  member/  academy/  content/  shop/  marketplace/  community/  assistant/
│   │   # chacun : ui/  viewmodel/  state/  navigation/
└── docs/
```

---

## 4. Responsabilités par couche

### 4.1 `ui/` (écrans Compose)
- Fonctions `@Composable` sans logique métier. Reçoivent un `UiState` et des lambdas (`onAction`).
- Deux niveaux : `XxxScreen` (branché au ViewModel via Hilt) et `XxxContent(state, onAction)` (pur, prévisualisable avec `@Preview`).
- Interdit : `launch`, appel réseau, accès disque, formatage de montant ou de date maison (utiliser `core:common`).

### 4.2 `viewmodel/` + `state/`
- Un ViewModel par écran. Expose **un seul** `StateFlow<XxxUiState>` et reçoit des `XxxUiAction`.
- Les événements ponctuels (navigation, toast) passent par un `Channel`/`SharedFlow` d'`UiEvent`.
- Appelle `UseCase` ou `Repository` (interfaces). Transforme `DsiResult` en `UiState`.
- Interdit : `Context`, `Retrofit`, `Room`, importer un DTO.

```kotlin
// state/BalanceUiState.kt
sealed interface BalanceUiState {
    data object Loading : BalanceUiState
    data class Success(val balance: Balance) : BalanceUiState
    data class Error(val error: AppError) : BalanceUiState
}
sealed interface BalanceUiAction { data object Retry : BalanceUiAction }
```

### 4.3 `:domain`
- `model/` : `data class` immuables, noms métier (`Balance`, `Subscription`), jamais le nom d'un DTO.
- `repository/` : interfaces. Retournent `DsiResult<T>` ou `Flow<DsiResult<T>>`.
- `usecase/` : uniquement pour combiner plusieurs sources ou porter une règle. **Pas de UseCase qui recopie un Repository.** Un seul verbe : `operator fun invoke`.

### 4.4 `:data:*`
- `api/` : interface Retrofit, **une fonction par route** de `CONTRATS-API.md`.
- `dto/` : `@Serializable`, tolérants : valeurs par défaut partout, `JsonElement` pour les champs « chaîne ou nombre », `ignoreUnknownKeys`.
- `mapper/` : DTO → modèle de domaine. **Toute normalisation** (nombres, dates, alias de champs) est faite ici, une seule fois.
- `local/` : DAO et entités Room de ce domaine.
- `XxxRepositoryImpl` : orchestre api + local + cache, traduit les erreurs avec `safeCall`.

### 4.5 `:core:*`
Briques techniques sans logique métier. `designsystem` ne connaît aucun modèle métier.

---

## 5. Résultats et erreurs (un seul type pour tout le projet)

```kotlin
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
- La table code d'erreur → `AppError` est dans `CONTRATS-API.md` §1.6. Elle est codée **une seule fois** dans `core:network` (`safeCall`).
- L'expiration de session est gérée **uniquement** par `SessionExpiredInterceptor` (purge + événement global → écran de connexion). **Aucun écran ni Repository ne teste un 401.**
- Les écrans traduisent `AppError` en texte via `strings.xml`. Jamais de message brut du serveur ni d'exception affichés.

---

## 6. Repository et cache

Politique de cache **choisie endpoint par endpoint**, dans le Repository, en une ligne lisible :

| Niveau | Quand | Outil |
|---|---|---|
| Aucun | paiement, retrait, achat, renouvellement | — |
| Mémoire + durée de vie | solde, profil, abonnement (ex. 30 s) | `MemoryCache` (`core:common`) |
| Disque | catalogues, conversations, progression, favoris | Room |

**Jamais en cache disque** : session, `accessProof`, `paymentTrackingToken`, URLs temporaires (`playback_url`, `read_url`, `read-url`).
Les listes affichées sont exposées en `Flow` pour que l'écran se mette à jour seul.

```kotlin
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

- **6 URLs de base** dans `WorkerUrls`, configurables par build (`BuildConfig`). Jamais en dur ailleurs.
- **Trois mécanismes d'authentification, jamais mélangés** : `Bearer` (Paiement), `X-Access-Proof` (4 autres Workers), `X-Payment-Tracking-Token` (1 route). Implémentés par intercepteurs OkHttp pilotés par une annotation sur la fonction Retrofit (`@Auth`).
- **Trois gabarits de réponse** (A Paiement, B enveloppe `{success,error,code,data}`, C Contenu) : décrits dans `CONTRATS-API.md` §1.3, désérialisés dans `:data`.
- Rate limit : `RateLimitGate` dans `core:network`. Les sondages se mettent en pause pendant `retryAfterSeconds`.
- Fichiers : multipart, jamais de `Content-Type` posé à la main.

---

## 8. Session et sécurité

- `SessionStore` (`core:session`) : jeton + état + `accessProof` **chiffrés** (DataStore + Keystore). Persistant si « se souvenir de moi », sinon mémoire.
- `accessProof` et `paymentTrackingToken` : chaînes opaques, jamais décodées ni journalisées.
- **Aucun secret, clé API ou identifiant de feuille dans l'application.**
- Journaux : jamais de jeton, mot de passe, numéro de paiement ni `accessProof`.
- `allowBackup="false"` ; pas de capture d'écran sur les écrans de lecture de contenu protégé (`FLAG_SECURE`), à confirmer avec l'architecte.

---

## 9. Design system (même visuel que le site)

- Tokens dans `core/designsystem/theme/`, copiés de `global.css` (voir `DESIGN-TOKENS.md`) : `DsiColors`, `DsiTypography`, `DsiShapes`, `DsiSpacing`, ombres, verre dépoli.
- Thèmes **clair et sombre**.
- Polices : **Fraunces** (titres, grands chiffres, jamais < 14 sp, jamais sur boutons/formulaires/tableaux), **Manrope** (corps, boutons, formulaires), **IBM Plex Mono** (petits libellés).
- Accent doré `#D4AF37` utilisé avec parcimonie (≈ 5 %). Rayons 6 / 10 / 16 dp.
- Tout composant réutilisé dans 2 écrans ou plus va dans `core:designsystem/components`.
- **Interdit** : `Color(0xFF…)`, `TextStyle(fontSize = …)`, `dp` d'espacement littéral dans `feature:*`. Toujours via le thème.
- Chaque `XxxContent` a au moins un `@Preview` clair et un sombre.
- Les écrans sont d'abord **fidèles au site** ; toute amélioration se discute avec l'architecte.

---

## 10. Textes et langues

- `strings.xml` par module, **FR (défaut) + EN**. Clés : `<feature>_<ecran>_<élément>` (ex. `member_balance_title`).
- Les messages d'erreur FR du site (cités dans `CONTRATS-API.md`) sont repris tels quels.
- Dates, montants, pluriels : via `core:common` (formateurs uniques).

---

## 11. Navigation

- Graphe racine dans `app/navigation`. Chaque `feature` expose **son** sous-graphe et ses routes typées (`feature/.../navigation`).
- Garde d'accès (`RequireAuth`) : si pas de session → écran de connexion. `SessionEvent.Expired` renvoie vers la connexion et vide la pile.
- Les `feature` ne se connaissent pas entre elles : la navigation inter-features passe par des routes déclarées dans `app`.

---

## 12. Données locales

Voir `CONTRATS-API.md` §8 (équivalences `localStorage` → Room/DataStore). Progression : 200 entrées max, purge des plus anciennes. Notifications locales : 50 max.

---

## 13. Médias

- Audio/vidéo : Media3, **service de lecture au premier plan** (lecture en arrière-plan, notification média).
- Livraison de contenu : voir `CONTRATS-API.md` §3.7 (4 formes). `playback_url` / `read_url` donnés **tels quels** au lecteur, jamais téléchargés en mémoire.
- Téléchargement : `WorkManager`, écriture sur disque, jamais de chargement complet en mémoire.
- Appels : service de premier plan pendant l'appel, permission micro demandée au moment de l'appel.

---

## 14. Tests et contrôles automatiques (workflow GitHub Actions)

À chaque tâche, doivent passer :
1. **ktlint** et **detekt** (syntaxe, style, complexité).
2. **Compilation** de tous les modules.
3. **Tests unitaires** (ViewModels, mappers, Repository avec faux `Api`).
4. **Tests de contrat** : un JSON d'exemple par route → DTO → modèle (extrait de `CONTRATS-API.md`).
5. **Contrôle d'architecture** : échoue si `feature` importe `data`, si `domain` importe Android/Retrofit, si un écran importe un DTO ou une API.
6. **Contrôle de contenu interdit** : échoue si `Color(0x…)` ou chaîne en dur dans `feature:*`.

Un ViewModel sans test = tâche non terminée. Les APK ne sont construits qu'à l'étape prévue par l'architecte.

---

## 15. Conventions de nommage

| Élément | Convention | Exemple |
|---|---|---|
| Écran | `XxxScreen` / `XxxContent` | `BalanceScreen` |
| ViewModel | `XxxViewModel` | `BalanceViewModel` |
| État / action / événement | `XxxUiState` / `XxxUiAction` / `XxxUiEvent` | `BalanceUiState` |
| Repository | interface `XxxRepository`, impl `XxxRepositoryImpl` | `PaymentRepository` |
| API Retrofit | `XxxApi` | `PaymentApi` |
| DTO / mapper | `XxxDto` / `XxxDto.toDomain()` | `BalanceDto` |
| UseCase | verbe + nom + `UseCase` | `LoadMemberDashboardUseCase` |
| Chaînes | `feature_ecran_element` | `member_balance_title` |

Un fichier = une classe principale. Fichiers de moins de ~300 lignes ; au-delà, découper.
Code et commentaires en **français** pour les commentaires métier, identifiants en **anglais** (sauf termes du contrat : `accessProof`, `idRetrait`…).

---

## 16. Interdits absolus (motif de rejet immédiat)

- Appel réseau, Room ou `Context` depuis un écran ou un ViewModel.
- Import de `:data:*` depuis `:feature:*`.
- Route, champ, code d'erreur ou statut absent de `CONTRATS-API.md`.
- Cache (mémoire/disque) dans un ViewModel ou un écran.
- Couleur, taille de police ou texte en dur.
- `GlobalScope`, `runBlocking` hors tests, `!!` non justifié.
- Test d'un 401 hors de l'intercepteur central.
- Journaliser un jeton, un mot de passe, un `accessProof`.
- Modifier un fichier non listé dans la fiche de tâche.
- Ajouter une dépendance Gradle sans accord.
- Rendre des morceaux de code : **toujours des fichiers complets**.

---

## 17. En cas de doute

- Un point du contrat est [À CONFIRMER], ambigu ou contradictoire → **s'arrêter et poser la question**. Ne jamais combler un trou par une invention.
- Une règle de ce document semble empêcher la tâche → **ne pas la contourner** ; signaler le conflit.
- Besoin d'un fichier non listé → le demander, ne pas le créer.

---

## 18. Définition de « terminé » (checklist de chaque tâche)

- [ ] Les fichiers de la fiche existent, complets, sans autre fichier modifié.
- [ ] Le workflow GitHub Actions est vert.
- [ ] États couverts : chargement, succès, erreur, session expirée, vide.
- [ ] Aucune valeur visuelle ni texte en dur.
- [ ] `@Preview` clair + sombre.
- [ ] Tests écrits et verts (ViewModel / mapper / contrat).
- [ ] Aucune règle de la section 16 violée.
- [ ] Compte rendu court : fichiers créés, points [À CONFIRMER] rencontrés, questions.