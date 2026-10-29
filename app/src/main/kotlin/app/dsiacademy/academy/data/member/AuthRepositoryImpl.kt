package app.dsiacademy.academy.data.member

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.network.safeCall
import app.dsiacademy.academy.data.member.api.AuthApi
import app.dsiacademy.academy.data.member.dto.ConnexionRequestDto
import app.dsiacademy.academy.data.member.dto.InscriptionRequestDto
import app.dsiacademy.academy.domain.member.AuthRepository
import app.dsiacademy.academy.domain.member.InscriptionCreated
import app.dsiacademy.academy.domain.member.InscriptionForm
import app.dsiacademy.academy.domain.member.LoginSuccess
import app.dsiacademy.academy.domain.member.PendingPayment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun connecter(
        identifiant: String,
        motDePasse: String,
    ): DsiResult<Any> {
        val result = safeCall {
            api.connecter(ConnexionRequestDto(identifiant = identifiant, motDePasse = motDePasse))
        }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val dto = result.data
                // Style A : erreur métier possible en HTTP 200 avec { "erreur": "..." }
                val erreur = dto.erreur ?: dto.message
                if (!erreur.isNullOrBlank() && dto.status == null && dto.token == null) {
                    return DsiResult.Error(
                        AppError.Http(code = "AUTH_ERROR", message = erreur, httpStatus = 200)
                    )
                }
                when {
                    dto.status == "connecte" && dto.authenticated == true && !dto.token.isNullOrBlank() -> {
                        DsiResult.Success(
                            LoginSuccess(
                                token = dto.token,
                                accountStatus = dto.accountStatus,
                                subscriptionStatus = dto.subscriptionStatus,
                                subscriptionActive = dto.subscriptionActive ?: false,
                                dateFin = dto.dateFin,
                                accessProof = dto.accessProof,
                            )
                        )
                    }
                    dto.status == "pending_payment" -> {
                        DsiResult.Success(
                            PendingPayment(
                                userId = dto.userId,
                                paymentTrackingToken = dto.paymentTrackingToken,
                                paymentUrl = dto.paymentUrl,
                            )
                        )
                    }
                    else -> {
                        DsiResult.Error(
                            AppError.Http(
                                code = "AUTH_ERROR",
                                message = erreur ?: "Identifiant ou mot de passe incorrect.",
                                httpStatus = 200,
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun creerInscription(form: InscriptionForm): DsiResult<InscriptionCreated> {
        val result = safeCall {
            api.creerInscription(
                InscriptionRequestDto(
                    nom = form.nom,
                    prenom = form.prenom,
                    whatsapp = form.whatsapp,
                    email = form.email,
                    pays = form.pays,
                    codeParrain = form.codeParrain,
                    motDePasse = form.motDePasse,
                )
            )
        }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val dto = result.data
                val erreur = dto.erreur ?: dto.message
                if (!erreur.isNullOrBlank() && dto.paymentUrl == null) {
                    DsiResult.Error(
                        AppError.Http(code = "INSCRIPTION_ERROR", message = erreur, httpStatus = 200)
                    )
                } else {
                    DsiResult.Success(
                        InscriptionCreated(
                            userId = dto.userId,
                            paymentTrackingToken = dto.paymentTrackingToken,
                            paymentUrl = dto.paymentUrl,
                        )
                    )
                }
            }
        }
    }

    override suspend fun verifierStatutPaiement(paymentTrackingToken: String): DsiResult<String> {
        val result = safeCall {
            api.verifierStatutPaiement(paymentTrackingToken)
        }
        return when (result) {
            is DsiResult.Error -> result
            is DsiResult.Success -> {
                val dto = result.data
                val erreur = dto.erreur ?: dto.message
                if (!erreur.isNullOrBlank() && dto.status == null) {
                    DsiResult.Error(
                        AppError.Http(code = "PAIEMENT_ERROR", message = erreur, httpStatus = 200)
                    )
                } else {
                    DsiResult.Success(dto.status ?: "inconnu")
                }
            }
        }
    }
}
