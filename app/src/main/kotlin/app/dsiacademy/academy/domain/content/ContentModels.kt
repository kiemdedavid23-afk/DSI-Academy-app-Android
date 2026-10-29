package app.dsiacademy.academy.domain.content

data class ContentItem(
    val id: String,
    val title: String,
    val slug: String?,
    val type: String,
    val summary: String?,
    val coverUrl: String?,
    val durationSeconds: Int? = null,
)

data class FormationNode(
    val id: String,
    val title: String,
    val type: String,
    val children: List<FormationNode> = emptyList(),
)

data class ContentPage(
    val items: List<ContentItem>,
    val hasMore: Boolean,
)


data class ContentCategory(
    val id: String,
    val name: String,
    val slug: String?,
    val count: Int,
)
