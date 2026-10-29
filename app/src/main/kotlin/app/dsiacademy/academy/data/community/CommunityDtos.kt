package app.dsiacademy.academy.data.community

import kotlinx.serialization.Serializable

/** Enveloppe style B — Worker Communauté. */
@Serializable
data class CommunityEnvelope(
    val success: Boolean = false,
    val error: String? = null,
    val code: String? = null,
    val data: CommunityMeData? = null,
)

@Serializable
data class CommunityMeData(
    val profile: CommunityProfileDto? = null,
    val communityStatus: String? = null,
    val needsProfile: Boolean? = null,
    val userId: String? = null,
)

@Serializable
data class CommunityProfileDto(
    val displayName: String? = null,
    val pseudonym: String? = null,
    val avatarUrl: String? = null,
    val avatarThumbUrl: String? = null,
    val avatarFileId: String? = null,
)
