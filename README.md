# DSI Academy — Application Android

Application Android officielle de **DSI Academy** (Kotlin + Jetpack Compose).

## Référence absolue

- **Visuel & logique métier** : le site `dsiacademy--main` (React).
- **Architecture** : `docs/ARCHITECTURE.md`
- **Contrats API** : `docs/CONTRATS-API.md`
- **Design tokens** : `docs/DESIGN-TOKENS.md` + `global.css` du site

Aucune valeur visuelle ou route API n'est inventée. Tout est dérivé du site.

## Stack

| Couche | Technologie |
|--------|-------------|
| UI | Jetpack Compose + Material 3 (thème custom DSI) |
| Architecture | Clean Architecture (paquets feature / domain / data / core) |
| DI | Hilt |
| Navigation | Navigation Compose |
| Réseau | Retrofit + OkHttp + kotlinx.serialization |
| Local | Room + DataStore |
| Médias | Media3 (ExoPlayer) + Coil |
| Min SDK | 24 |

## Structure

```
app/src/main/kotlin/app/dsiacademy/academy/
├── DsiApplication.kt
├── MainActivity.kt
├── navigation/          # NavHost racine
├── di/                  # Modules Hilt
├── core/
│   ├── common/          # DsiResult, AppError (Kotlin pur)
│   ├── designsystem/    # Thème, couleurs, typo, shapes, spacing
│   ├── network/         # WorkerUrls, clients Retrofit (à venir)
│   ├── session/         # SessionStore chiffré (à venir)
│   ├── database/        # Room (à venir)
│   └── media/           # ExoPlayer service (à venir)
├── domain/              # Interfaces + modèles métier (Kotlin pur)
├── data/                # Implémentations (API + local + mappers)
└── feature/             # Écrans Compose
    ├── public/          # Splash, Welcome, Login, Register…
    ├── member/
    ├── academy/
    ├── content/
    ├── shop/
    ├── marketplace/
    ├── community/
    └── assistant/
```

## État actuel (fondations)

- [x] Gradle (version catalog, module unique `:app`)
- [x] Design system (couleurs, typo, shapes, spacing) strictement aligné sur `global.css`
- [x] Polices embarquées (Fraunces, Manrope, IBM Plex Mono — SIL OFL)
- [x] `DsiResult` / `AppError`
- [x] Splash + Welcome (écrans publics minimaux)
- [x] Navigation de base
- [x] SessionStore chiffré (Keystore) + intercepteurs OkHttp (Bearer / AccessProof / SessionExpired)
- [x] Auth : Login + Inscription (API + UI alignés site)
- [x] Espace membre : shell + dashboard home
- [ ] Features métier (profil, contenu, shop…)

## Build

```bash
./gradlew :app:assembleDebug
```

## Règles d'or (rappel)

1. Un écran ne connaît que son ViewModel.
2. Un ViewModel ne connaît que des interfaces `domain`.
3. Tout le cache vit dans les Repository (`data`).
4. `domain` et `core/common` sont du Kotlin pur.
5. Aucune valeur visuelle en dur — uniquement les tokens.
6. Aucune route inventée — uniquement `CONTRATS-API.md`.
7. Une tâche = les fichiers listés dans sa fiche.
