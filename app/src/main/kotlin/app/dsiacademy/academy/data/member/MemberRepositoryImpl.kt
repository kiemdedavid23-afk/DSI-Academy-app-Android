package app.dsiacademy.academy.data.member

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.network.safeCall
import app.dsiacademy.academy.data.member.api.MemberApi
import app.dsiacademy.academy.data.member.dto.asDouble
import app.dsiacademy.academy.data.member.dto.DemandeRetraitRequestDto
import app.dsiacademy.academy.data.member.dto.RenouvelerRequestDto
import app.dsiacademy.academy.domain.member.DemandeRetraitResult
import app.dsiacademy.academy.domain.member.PaiementItem
import app.dsiacademy.academy.domain.member.PaiementsPage
import app.dsiacademy.academy.domain.member.GiftCodeResult
import app.dsiacademy.academy.domain.member.RenouvelerResult
import app.dsiacademy.academy.domain.member.RetraitEvenement
import app.dsiacademy.academy.data.member.dto.UtiliserCodeRequestDto
import app.dsiacademy.academy.domain.member.RetraitItem
import app.dsiacademy.academy.domain.member.RetraitsList

import app.dsiacademy.academy.domain.member.MemberAbonnement
import app.dsiacademy.academy.domain.member.MemberDashboard
import app.dsiacademy.academy.domain.member.MemberParrainage
import app.dsiacademy.academy.domain.member.MemberProfil
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.MemberSolde
import app.dsiacademy.academy.domain.member.ParrainageLigne
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val api: MemberApi,
) : MemberRepository {

    override suspend fun getProfil(): DsiResult<MemberProfil> {
        val result = safeCall { api.getProfil() }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("PROFIL_ERROR", d.erreur, 200))
                } else {
                    DsiResult.Success(
                        MemberProfil(
                            prenom = d.prenom ?: d.firstName ?: "",
                            nom = d.nom ?: d.lastName ?: "",
                            identifiantDsi = d.identifiantDsi ?: d.idDsi ?: "",
                            niveau = d.niveauActuel ?: d.niveau ?: "Membre",
                            codeParrainage = d.codeParrainagePersonnel
                                ?: d.codeParrainage
                                ?: d.codeParrainageSnake
                                ?: "",
                            statut = d.statut ?: d.statutCompte ?: "ACTIF",
                            dateInscription = d.dateInscription ?: d.createdAt,
                            dateActivation = d.dateActivation ?: d.activatedAt,
                            whatsapp = d.whatsapp ?: d.telephone,
                            email = d.email,
                            pays = d.pays,
                        )
                    )
                }
            }
        }
    }

    override suspend fun getSolde(): DsiResult<MemberSolde> {
        val result = safeCall { api.getSolde() }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("SOLDE_ERROR", d.erreur, 200))
                } else {
                    DsiResult.Success(
                        MemberSolde(
                            identifiantDsi = d.identifiantDsi,
                            soldeDsiActuel = d.soldeDsiActuel.asDouble(),
                            totalDsiGagnes = d.totalDsiGagnes.asDouble(),
                            dateMiseAJour = d.dateMiseAJour,
                        )
                    )
                }
            }
        }
    }

    override suspend fun getAbonnement(): DsiResult<MemberAbonnement> {
        val result = safeCall { api.getAbonnement() }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("ABONNEMENT_ERROR", d.erreur, 200))
                } else {
                    DsiResult.Success(
                        MemberAbonnement(
                            statut = d.statut,
                            dateDebut = d.dateDebut,
                            dateFin = d.dateFin,
                            formule = d.formule,
                            modePaiement = d.modePaiement,
                            renouvellements = d.renouvellements,
                        )
                    )
                }
            }
        }
    }

    override suspend fun getParrainage(): DsiResult<MemberParrainage> {
        val result = safeCall { api.getParrainage() }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("PARRAINAGE_ERROR", d.erreur, 200))
                } else {
                    DsiResult.Success(
                        MemberParrainage(
                            codeParrainagePersonnel = d.codeParrainagePersonnel ?: "",
                            filleulsDirectsTotal = d.filleulsDirectsTotal ?: 0,
                            filleulsDirectsActifs = d.filleulsDirectsActifs ?: 0,
                            recompensesNiveau1Dsi = d.recompensesNiveau1Dsi.asDouble(),
                            recompensesNiveau2Dsi = d.recompensesNiveau2Dsi.asDouble(),
                            totalDsiParrainage = d.totalDsiParrainage.asDouble(),
                            historique = (d.historique ?: emptyList()).map {
                                ParrainageLigne(
                                    type = it.type ?: "",
                                    montant = it.montant.asDouble(),
                                    identifiantDsiFilleul = it.identifiantDsiFilleul,
                                    date = it.date,
                                )
                            },
                        )
                    )
                }
            }
        }
    }

    override suspend fun getDashboard(): DsiResult<MemberDashboard> = coroutineScope {
        val profilDef = async { getProfil() }
        val soldeDef = async { getSolde() }
        val aboDef = async { getAbonnement() }
        val retraitsDef = async { getRetraits() }

        val profil = profilDef.await()
        if (profil is DsiResult.Error) return@coroutineScope profil

        val solde = soldeDef.await().getOrNull()
        val abo = aboDef.await().getOrNull()
        val retraits = retraitsDef.await().getOrNull()?.retraits ?: emptyList()

        DsiResult.Success(
            MemberDashboard(
                profil = (profil as DsiResult.Success).data,
                solde = solde,
                abonnement = abo,
                retraits = retraits,
            )
        )
    }

    override suspend fun getRetraits(): DsiResult<RetraitsList> {
        val result = safeCall { api.getRetraits() }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("RETRAITS_ERROR", d.erreur, 200))
                } else {
                    val liste = (d.retraits ?: emptyList()).map {
                        RetraitItem(
                            idRetrait = it.idRetrait ?: "",
                            statut = it.statut ?: "",
                            moyenPaiement = it.moyenPaiement,
                            montantDsiDemande = it.montantDsiDemande.asDouble(),
                            montantDsiNet = it.montantDsiNet.asDouble(),
                            montantFcfa = it.montantFcfa?.asDouble(),
                            dateDemande = it.dateDemande,
                        )
                    }
                    DsiResult.Success(RetraitsList(total = d.total ?: liste.size, retraits = liste))
                }
            }
        }
    }

    override suspend fun getMoyensRetrait(pays: String?): DsiResult<List<String>> {
        val result = safeCall { api.getMoyensRetrait(pays) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("MOYENS_ERROR", d.erreur, 200))
                } else {
                    DsiResult.Success(d.moyens ?: emptyList())
                }
            }
        }
    }

    override suspend fun demanderRetrait(
        montantDsi: Double,
        moyenPaiement: String,
        numeroPaiement: String,
    ): DsiResult<DemandeRetraitResult> {
        val result = safeCall {
            api.demanderRetrait(
                DemandeRetraitRequestDto(
                    montantDsi = montantDsi,
                    moyenPaiement = moyenPaiement,
                    numeroPaiement = numeroPaiement,
                )
            )
        }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                DsiResult.Success(
                    DemandeRetraitResult(
                        status = d.status ?: "refuse",
                        idRetrait = d.idRetrait,
                        message = d.message ?: d.erreur,
                        montantDsiNet = d.montantDsiNet?.asDouble(),
                        montantFcfaNet = d.montantFcfaNet?.asDouble(),
                    )
                )
            }
        }
    }

    override suspend fun getPaiements(page: Int, limit: Int): DsiResult<PaiementsPage> {
        val result = safeCall { api.getPaiements(page, limit) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("PAIEMENTS_ERROR", d.erreur, 200))
                } else {
                    val liste = (d.paiements ?: emptyList()).map {
                        PaiementItem(
                            idTransaction = it.idTransaction ?: "",
                            typePaiement = it.typePaiement,
                            statut = it.statut ?: "",
                            montant = it.montant.asDouble(),
                            devise = it.devise,
                            plateforme = it.plateforme,
                            dateCreation = it.dateCreation,
                            dateConfirmation = it.dateConfirmation,
                        )
                    }
                    DsiResult.Success(
                        PaiementsPage(
                            paiements = liste,
                            page = d.page ?: page,
                            totalPages = d.totalPages ?: 1,
                            total = d.total ?: liste.size,
                        )
                    )
                }
            }
        }
    }

    override suspend fun renouvelerAbonnement(modePaiement: String): DsiResult<RenouvelerResult> {
        val result = safeCall {
            api.renouvelerAbonnement(RenouvelerRequestDto(modePaiement = modePaiement))
        }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                DsiResult.Success(
                    RenouvelerResult(
                        status = d.status ?: "refuse",
                        paymentUrl = d.paymentUrl,
                        message = d.message ?: d.erreur,
                    )
                )
            }
        }
    }

    override suspend fun utiliserCode(code: String): DsiResult<GiftCodeResult> {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            return DsiResult.Success(GiftCodeResult.Failure("empty"))
        }
        val result = safeCall { api.utiliserCode(UtiliserCodeRequestDto(code = trimmed)) }
        return when (result) {
            is DsiResult.Error -> {
                if (result.error is AppError.SessionExpired) {
                    DsiResult.Success(GiftCodeResult.Failure("session"))
                } else {
                    DsiResult.Success(GiftCodeResult.Failure("unavailable"))
                }
            }
            is DsiResult.Success -> {
                val d = result.data
                val status = d.status?.lowercase()
                if (status == "succes" || status == "success") {
                    DsiResult.Success(
                        GiftCodeResult.Success(
                            montantDsi = d.montantDsi.asDouble(),
                            message = d.message,
                        )
                    )
                } else {
                    val msg = (d.message ?: d.erreur ?: "").lowercase()
                    val reason = when {
                        "déjà utilisé" in msg || "deja utilise" in msg || "already" in msg -> "alreadyUsed"
                        else -> "invalid"
                    }
                    DsiResult.Success(GiftCodeResult.Failure(reason))
                }
            }
        }
    }

    override suspend fun getRetraitHistorique(idRetrait: String): DsiResult<List<RetraitEvenement>> {
        val result = safeCall { api.getRetraitHistorique(idRetrait) }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val d = result.data
                if (!d.erreur.isNullOrBlank()) {
                    DsiResult.Error(AppError.Http("HISTO_ERROR", d.erreur, 200))
                } else {
                    DsiResult.Success(
                        (d.evenements ?: emptyList()).map {
                            RetraitEvenement(
                                date = it.date,
                                champ = it.champ,
                                ancienneValeur = it.ancienneValeur,
                                nouvelleValeur = it.nouvelleValeur,
                            )
                        }
                    )
                }
            }
        }
    }
}
