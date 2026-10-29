package app.dsiacademy.academy.domain.member

import app.dsiacademy.academy.core.common.DsiResult

interface MemberRepository {
    suspend fun getProfil(): DsiResult<MemberProfil>
    suspend fun getSolde(): DsiResult<MemberSolde>
    suspend fun getAbonnement(): DsiResult<MemberAbonnement>
    suspend fun getParrainage(): DsiResult<MemberParrainage>
    suspend fun getDashboard(): DsiResult<MemberDashboard>

    suspend fun getRetraits(): DsiResult<RetraitsList>
    suspend fun getMoyensRetrait(pays: String?): DsiResult<List<String>>
    suspend fun demanderRetrait(
        montantDsi: Double,
        moyenPaiement: String,
        numeroPaiement: String,
    ): DsiResult<DemandeRetraitResult>

    suspend fun getPaiements(page: Int = 1, limit: Int = 20): DsiResult<PaiementsPage>

    suspend fun renouvelerAbonnement(modePaiement: String): DsiResult<RenouvelerResult>
    suspend fun utiliserCode(code: String): DsiResult<GiftCodeResult>
    suspend fun getRetraitHistorique(idRetrait: String): DsiResult<List<RetraitEvenement>>
}
