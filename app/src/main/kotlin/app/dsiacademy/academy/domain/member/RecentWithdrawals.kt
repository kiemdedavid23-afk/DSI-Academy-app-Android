package app.dsiacademy.academy.domain.member

import app.dsiacademy.academy.core.common.DsiFormats

/**
 * Comme `chargerRetraits` du site : tri par dateDemande décroissante,
 * dates absentes / illisibles en fin de liste, puis limite.
 * Tri stable pour dates égales.
 */
fun recentWithdrawals(
    items: List<RetraitItem>,
    limit: Int = 5,
): List<RetraitItem> {
    if (items.isEmpty() || limit <= 0) return emptyList()
    return items
        .withIndex()
        .sortedWith(
            compareByDescending<IndexedValue<RetraitItem>> { (_, item) ->
                DsiFormats.parseIsoMs(item.dateDemande) ?: Long.MIN_VALUE
            }.thenBy { it.index },
        )
        .map { it.value }
        .take(limit)
}
