package app.dsiacademy.academy.data.community

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.network.safeCall
import app.dsiacademy.academy.domain.community.CommunityProfile
import javax.inject.Inject
import javax.inject.Singleton

interface CommunityRepository {
    suspend fun getMyProfile(): DsiResult<CommunityProfile?>
}

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val api: CommunityApi,
) : CommunityRepository {
    override suspend fun getMyProfile(): DsiResult<CommunityProfile?> {
        val result = safeCall { api.getMe() }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val env = result.data
                if (!env.success) {
                    val code = env.code
                    if (code in listOf("ACCESS_PROOF_MISSING", "ACCESS_PROOF_INVALID", "ACCESS_PROOF_EXPIRED")) {
                        DsiResult.Error(AppError.SessionExpired(env.error))
                    } else {
                        // needsProfile / incomplete → pas d'erreur, juste null
                        DsiResult.Success(null)
                    }
                } else {
                    val p = env.data?.profile
                    if (p == null) {
                        DsiResult.Success(null)
                    } else {
                        DsiResult.Success(
                            CommunityProfile(
                                displayName = p.displayName,
                                pseudonym = p.pseudonym,
                                avatarThumbUrl = p.avatarThumbUrl ?: p.avatarUrl,
                                avatarUrl = p.avatarUrl,
                            ),
                        )
                    }
                }
            }
        }
    }
}
