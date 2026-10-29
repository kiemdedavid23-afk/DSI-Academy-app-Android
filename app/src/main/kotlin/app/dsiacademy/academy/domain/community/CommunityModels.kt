package app.dsiacademy.academy.domain.community

data class CommunityProfile(
    val displayName: String?,
    val pseudonym: String?,
    /** URL courte durée — ne jamais persister sur disque. */
    val avatarThumbUrl: String?,
    val avatarUrl: String?,
)
