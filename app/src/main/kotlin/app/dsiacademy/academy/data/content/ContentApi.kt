package app.dsiacademy.academy.data.content

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApi {
    @GET("content")
    suspend fun getCatalog(
        @Query("type") type: String,
        @Query("limit") limit: Int = 30,
        @Query("category_id") categoryId: String? = null,
        @Query("cursor_created_at") cursorCreatedAt: String? = null,
        @Query("cursor_id") cursorId: String? = null,
    ): ContentCatalogDto

    @GET("content/{id}")
    suspend fun getById(@Path("id") id: String): ContentItemDto

    @GET("content/{id}/components/tree")
    suspend fun getTree(@Path("id") id: String): FormationTreeDto

    @GET("content/{id}/assets/best")
    suspend fun getBestAsset(@Path("id") id: String): BestAssetDto


    @GET("content/categories")
    suspend fun getCategories(@Query("type") type: String): CategoriesDto

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
    ): SearchDto
}
