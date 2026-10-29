# DESIGN-TOKENS.md — DSI Academy Android

> Règle unique : **l'app doit avoir le même visuel que le site.** Même charte, mêmes couleurs, mêmes polices, mêmes rayons, même ambiance.
> Source de vérité : `src/assets/styles/global.css` du site (charte « v3.0 institutionnelle : 70 % neutres / 20 % sombre / 5 % or »), puis le fichier `.css` à côté de chaque écran du site.
> Ce document ne contient aucun code. Grok traduit ces valeurs dans `core/designsystem/theme/` et nulle part ailleurs (voir ARCHITECTURE.md §9).
> Statut : v1.0. Les points marqués **[À CONFIRMER]** ne sont pas tranchés : s'arrêter et poser la question.

---

## 1. Couleurs — thème clair (défaut)

| Rôle | Valeur | Nom du token (site) |
|---|---|---|
| Fond de l'app | `#F8FAFC` | `--bg-app` |
| Surface (cartes, feuilles) | `#FFFFFF` | `--bg-surface` |
| Surface alternative | `#F1F5F9` | `--bg-surface-alt` |
| Fond sombre premium | `#0F172A` | `--bg-dark-premium` |
| Carte sombre | `#1E293B` | `--bg-dark-card` |
| Texte principal | `#0F172A` | `--text-primary` |
| Texte secondaire | `#475569` | `--text-secondary` |
| Texte discret | `#94A3B8` | `--text-muted` |
| Texte sur fond sombre | `#F8FAFC` | `--text-on-dark` |
| Or prestige (accent) | `#D4AF37` | `--gold-prestige` |
| Or clair (fond d'accent) | `#FDF6E2` | `--gold-light` |
| Bordure or | `#D4AF37` à 35 % d'opacité | `--gold-border` |
| Bleu institutionnel | `#1E40AF` | `--blue-primary` |
| Bleu clair | `#EFF6FF` | `--blue-light` |
| Bordure claire | `#E2E8F0` | `--border-light` |

## 2. Couleurs — thème sombre

Seules ces valeurs changent. Tout le reste (or, bleu institutionnel) reste identique au thème clair.

| Rôle | Valeur sombre |
|---|---|
| Fond de l'app | `#0B1120` |
| Surface | `#111827` |
| Surface alternative | `#1E293B` |
| Fond sombre premium | `#020617` |
| Carte sombre | `#0F172A` |
| Texte principal | `#F8FAFC` |
| Texte secondaire | `#CBD5E1` |
| Texte discret | `#94A3B8` |
| Bordure | `#334155` |
| Bleu clair | `#1E3A5F` |

**[À CONFIRMER]** : le site ne redéfinit pas l'« or clair » (`#FDF6E2`) en sombre, donc un fond crème apparaît sur fond sombre. Probablement un oubli du site. Ne pas le copier : demander quelle valeur sombre utiliser.

## 3. Accent doré

- L'or reste **rare : environ 5 % de l'écran**. Jamais en grande surface.
- Dégradé de barre de progression : de `#D4AF37` vers `#E8C77E`, de gauche à droite.
- Un élément « actif ou favori » prend le fond or clair et la bordure or.

## 4. Couleurs sémantiques (erreur, succès, alerte)

Le site n'en déclare aucune dans `global.css`. Elles sont écrites en dur dans les CSS d'écran. D'après leur fréquence d'usage, les valeurs probables sont :

| Rôle | Valeur probable |
|---|---|
| Erreur / danger | `#B91C1C` (texte), `#FCA5A5` (bordure ou fond léger) |
| Succès | `#15803D` (texte), `#86EFAC` (bordure ou fond léger) |
| Alerte | `#B45309` |
| Information / lien | `#2563EB`, `#1D4ED8` |

**[À CONFIRMER]** : valeurs déduites du comptage, non de l'intention du site. Avant de les figer, Grok liste pour chaque écran quelle couleur le site utilise réellement pour l'erreur et le succès, et l'architecte tranche. Puisque les littéraux sont interdits dans `feature`, ces rôles **doivent** exister comme tokens.

## 5. Typographie

| Police | Usage | Graisses utilisées par le site |
|---|---|---|
| **Fraunces** (serif, « royal ») | salutations, titres, grands chiffres | 400, 500, 600, 700 + italiques 400, 500, 600 |
| **Manrope** (sans) | tout le reste : corps, boutons, formulaires, tableaux | 400, 500, 600, 700, 800 |
| **IBM Plex Mono** | petits libellés techniques | 500 |

Règles de la charte, à respecter telles quelles :
- Fraunces : **jamais en dessous de 14 sp**, **jamais** sur un bouton, un formulaire ou un tableau.
- Manrope est la police par défaut de toute l'app.
- Le site charge les polices depuis Google Fonts. **L'app doit les embarquer** (hors ligne, mobiles d'entrée de gamme). **[À CONFIRMER]** avec l'architecte : taille de l'APK et licence (toutes trois sont sous licence libre SIL OFL).
- Le site ne définit pas d'échelle de tailles. Grok reprend la taille exacte du CSS de l'écran correspondant (1 rem = 16 sp) et la range dans l'échelle `DsiTypography`, sans inventer de taille.

## 6. Rayons

| Token | Valeur |
|---|---|
| Petit | 6 dp |
| Moyen | 10 dp |
| Grand | 16 dp |
| Plein (pilules, puces) | arrondi complet |
| Rond (avatars) | 50 % |

Le site utilise surtout moyen (cartes, champs) et grand (grandes cartes, feuilles). Éviter tout rayon hors de cette liste. Le site contient quelques `12px` isolés : ne pas les reprendre, utiliser le moyen ou le grand.

## 7. Ombres

| Token | Thème clair | Thème sombre |
|---|---|---|
| Petite | 0 1 2, noir bleuté 5 % | 0 1 2, noir 35 % |
| Carte | 0 4 6 puis 0 2 4, noir bleuté 5 % et 3 % | 0 8 24, noir 45 % |
| Survol / appui | 0 10 15 puis 0 4 6, 8 % et 3 % | 0 12 28, noir 55 % |

Sur Android, l'ombre s'exprime par l'élévation. Elle doit rester discrète comme sur le site. **[À CONFIRMER]** : valeurs d'élévation en dp à valider visuellement sur appareil.

## 8. Verre dépoli (accueil membre, barres, feuilles)

| | Clair | Sombre |
|---|---|---|
| Fond | blanc à 86 % | `#0F172A` à 78 % |
| Bordure | blanc à 55 % | `#94A3B8` à 25 % |
| Flou | environ 10 | environ 10 |

Le flou en temps réel est coûteux sur mobiles d'entrée de gamme (minSdk 24). Sur Android 11 et moins, **remplacer le flou par le fond semi-opaque seul**, sans flou. **[À CONFIRMER]**.

## 9. Espacement

Le site n'a pas de jetons d'espacement : il écrit des valeurs en rem. Échelle proposée pour l'app (1 rem = 16 dp) :

| Token | Valeur |
|---|---|
| xs | 4 dp |
| sm | 8 dp |
| md | 12 dp |
| lg | 16 dp |
| xl | 20 dp (marge latérale d'écran, comme les 1,25 rem du site) |
| xxl | 24 dp |
| xxxl | 32 dp |

Les valeurs du site qui tombent entre deux paliers (0,85 rem, 1,15 rem) sont arrondies au palier le plus proche. **[À CONFIRMER]**.

## 10. Mouvement

Le site respecte une préférence « réduire les animations » qui coupe toutes les animations et transitions. L'app doit honorer à la fois ce réglage du site (réglage de l'espace membre) et le réglage système d'accessibilité.

---

## 11. Contrôle de fidélité (ce que l'architecte vérifie sur chaque livraison de Grok)

1. **Chaque fiche de tâche cite le fichier source du site** (écran `.jsx` et son `.css`). Grok les lit avant d'écrire une ligne.
2. **Aucun littéral visuel dans `feature`.** Tout passe par les tokens de ce document.
3. **Aucune valeur inventée.** Si le CSS du site n'a pas de réponse, Grok pose la question.
4. **Les 4 états** (chargement, vide, erreur, succès) existent et ressemblent à ceux du site (squelettes, messages FR repris tels quels).
5. **Comparaison côte à côte.** Pour chaque écran livré : capture de l'app (clair et sombre) à côté de la capture du site en vue mobile. L'architecte ne valide pas un écran sans cette paire.
6. **Hiérarchie respectée** : or rare, Fraunces seulement pour les titres, Manrope partout ailleurs, rayons 6 / 10 / 16.
7. **Accessibilité** : contraste du texte discret (`#94A3B8` sur fond clair) à vérifier. Il est faible et ne doit pas porter d'information essentielle.

## 12. Points à trancher avant le premier écran

1. Valeur sombre de l'or clair (§2).
2. Couleurs d'erreur, succès, alerte (§4).
3. Embarquement des polices dans l'APK (§5).
4. Élévations en dp (§7).
5. Flou sur Android 11 et moins (§8).
6. Échelle d'espacement (§9).
