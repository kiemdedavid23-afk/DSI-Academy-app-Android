package app.dsiacademy.academy.data.content

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.network.safeCall
import app.dsiacademy.academy.domain.content.ContentCategory
import app.dsiacademy.academy.domain.content.ContentItem
import app.dsiacademy.academy.domain.content.ContentPage
import app.dsiacademy.academy.domain.content.FormationNode
import javax.inject.Inject
import javax.inject.Singleton

interface ContentRepository {
    suspend fun getCatalog(type: String, limit: Int = 30, categoryId: String? = null): DsiResult<ContentPage>
    suspend fun getById(id: String): DsiResult<ContentItem>
    suspend fun getTree(id: String): DsiResult<FormationNode>
    suspend fun isDownloadable(id: String): Boolean
    suspend fun getCategories(type: String): DsiResult<List<ContentCategory>>
    suspend fun search(query: String, type: String?, limit: Int = 30): DsiResult<ContentPage>
}

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: ContentApi,
) : ContentRepository {

    private fun mapItem(it: ContentItemDto, fallbackType: String = "book") = ContentItem(
        id = it.id ?: "",
        title = it.title ?: "Sans titre",
        slug = it.slug,
        type = it.type ?: fallbackType,
        summary = it.summary ?: it.description,
        coverUrl = it.coverUrl ?: it.cover_url,
        durationSeconds = null,
    )

    override suspend fun getCatalog(type: String, limit: Int, categoryId: String?): DsiResult<ContentPage> {
        val result = safeCall { api.getCatalog(type = type, limit = limit, categoryId = categoryId) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                val items = (d.items ?: emptyList()).map { mapItem(it, type) }
                DsiResult.Success(ContentPage(items = items, hasMore = d.nextCursor != null))
            }
        }
    }

    override suspend fun getById(id: String): DsiResult<ContentItem> {
        val result = safeCall { api.getById(id) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> DsiResult.Success(mapItem(result.data))
        }
    }

    override suspend fun getTree(id: String): DsiResult<FormationNode> {
        val result = safeCall { api.getTree(id) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> DsiResult.Success(mapNode(result.data))
        }
    }

    override suspend fun isDownloadable(id: String): Boolean
    suspend fun getCategories(type: String): DsiResult<List<ContentCategory>>
    suspend fun search(query: String, type: String?, limit: Int = 30): DsiResult<ContentPage> {
        return try {
            val r = safeCall { api.getBestAsset(id) }
            (r as? DsiResult.Success)?.data?.downloadable == true
        } catch (_: Exception) {
            false
        }
    }

    private fun mapNode(dto: FormationTreeDto): FormationNode = FormationNode(
        id = dto.id ?: "",
        title = dto.title ?: "Formation",
        type = dto.type ?: "training",
        children = (dto.children ?: emptyList()).map { mapChild(it) },
    )

    private fun mapChild(n: FormationNodeDto): FormationNode {
        val c = n.content
        return FormationNode(
            id = c?.id ?: "",
            title = c?.title ?: "Module",
            type = c?.type ?: "book",
            children = (n.children ?: emptyList()).map { mapChild(it) },
        )
    }

    override suspend fun getCategories(type: String): DsiResult<List<ContentCategory>> {
        val result = safeCall { api.getCategories(type) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val list = (result.data.items ?: emptyList()).map {
                    ContentCategory(
                        id = it.id ?: "",
                        name = it.name ?: "",
                        slug = it.slug,
                        count = it.count ?: 0,
                    )
                }
                DsiResult.Success(list)
            }
        }
    }

    override suspend fun search(query: String, type: String?, limit: Int): DsiResult<ContentPage> {
        val result = safeCall { api.search(query = query, type = type, limit = limit) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val items = (result.data.items ?: emptyList()).map { mapItem(it, type ?: "book") }
                DsiResult.Success(ContentPage(items = items, hasMore = false))
            }
        }
    }
}
