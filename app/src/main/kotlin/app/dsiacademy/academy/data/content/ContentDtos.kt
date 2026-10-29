package app.dsiacademy.academy.data.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentCatalogDto(
    val items: List<ContentItemDto>? = null,
    @SerialName("next_cursor") val nextCursor: ContentCursorDto? = null,
)

@Serializable
data class ContentCursorDto(
    @SerialName("cursor_created_at") val cursorCreatedAt: String? = null,
    @SerialName("cursor_id") val cursorId: String? = null,
)

@Serializable
data class ContentItemDto(
    val id: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val type: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val cover_url: String? = null,
    val coverUrl: String? = null,
    val category_id: String? = null,
    val categoryId: String? = null,
)

@Serializable
data class BestAssetDto(
    val downloadable: Boolean? = null,
)

@Serializable
data class FormationTreeDto(
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val children: List<FormationNodeDto>? = null,
)

@Serializable
data class FormationNodeDto(
    val component: FormationComponentDto? = null,
    val content: ContentItemDto? = null,
    val children: List<FormationNodeDto>? = null,
)

@Serializable
data class FormationComponentDto(
    val id: String? = null,
    val position: Int? = null,
)


@Serializable
data class CategoriesDto(
    val items: List<CategoryDto>? = null,
)

@Serializable
data class CategoryDto(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val count: Int? = null,
)

@Serializable
data class SearchDto(
    val query: String? = null,
    val items: List<ContentItemDto>? = null,
    val limit: Int? = null,
    val offset: Int? = null,
)
