package app.dsiacademy.academy.data.member.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.contentOrNull

@Serializable
data class ProfilDto(
    val prenom: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    val nom: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val identifiantDsi: String? = null,
    @SerialName("id_dsi") val idDsi: String? = null,
    val niveauActuel: String? = null,
    val niveau: String? = null,
    val codeParrainagePersonnel: String? = null,
    val codeParrainage: String? = null,
    @SerialName("code_parrainage") val codeParrainageSnake: String? = null,
    val statut: String? = null,
    @SerialName("statut_compte") val statutCompte: String? = null,
    val dateInscription: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val dateActivation: String? = null,
    @SerialName("activated_at") val activatedAt: String? = null,
    val whatsapp: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    val pays: String? = null,
    val erreur: String? = null,
)

@Serializable
data class SoldeDto(
    val identifiantDsi: String? = null,
    val soldeDsiActuel: JsonElement? = null,
    val totalDsiGagnes: JsonElement? = null,
    val dateMiseAJour: String? = null,
    val erreur: String? = null,
)

@Serializable
data class AbonnementDto(
    val statut: String? = null,
    val dateDebut: String? = null,
    val dateFin: String? = null,
    val formule: String? = null,
    val modePaiement: String? = null,
    val renouvellements: Int? = null,
    val erreur: String? = null,
)

@Serializable
data class ParrainageDto(
    val codeParrainagePersonnel: String? = null,
    val filleulsDirectsTotal: Int? = null,
    val filleulsDirectsActifs: Int? = null,
    val recompensesNiveau1Dsi: JsonElement? = null,
    val recompensesNiveau2Dsi: JsonElement? = null,
    val totalDsiParrainage: JsonElement? = null,
    val historique: List<ParrainageHistoriqueDto>? = null,
    val historiqueHasMore: Boolean? = null,
    val erreur: String? = null,
)

@Serializable
data class ParrainageHistoriqueDto(
    val type: String? = null,
    val montant: JsonElement? = null,
    val identifiantDsiFilleul: String? = null,
    val date: String? = null,
)

fun JsonElement?.asDouble(default: Double = 0.0): Double {
    if (this == null) return default
    val p = this as? JsonPrimitive ?: return default
    return p.doubleOrNull ?: p.contentOrNull?.toDoubleOrNull() ?: default
}

fun JsonElement?.asInt(default: Int = 0): Int = asDouble(default.toDouble()).toInt()

@Serializable
data class RetraitsResponseDto(
    val total: Int? = null,
    val retraits: List<RetraitDto>? = null,
    val erreur: String? = null,
)

@Serializable
data class RetraitDto(
    val idRetrait: String? = null,
    val statut: String? = null,
    val moyenPaiement: String? = null,
    val montantDsiDemande: JsonElement? = null,
    val dateDemande: String? = null,
    val pays: String? = null,
    val montantFcfa: JsonElement? = null,
    val montantDsiNet: JsonElement? = null,
    val contributionStabiliteDsi: JsonElement? = null,
)

@Serializable
data class MoyensRetraitDto(
    val pays: String? = null,
    val moyens: List<String>? = null,
    val erreur: String? = null,
)

@Serializable
data class DemandeRetraitRequestDto(
    val montantDsi: Double,
    val moyenPaiement: String,
    val numeroPaiement: String,
)

@Serializable
data class DemandeRetraitResponseDto(
    val status: String? = null,
    val idRetrait: String? = null,
    val montantDsiNet: JsonElement? = null,
    val montantFcfaNet: JsonElement? = null,
    val message: String? = null,
    val erreur: String? = null,
)

@Serializable
data class PaiementsResponseDto(
    val paiements: List<PaiementDto>? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val total: Int? = null,
    val totalPages: Int? = null,
    val erreur: String? = null,
)

@Serializable
data class PaiementDto(
    val idTransaction: String? = null,
    val typePaiement: String? = null,
    val statut: String? = null,
    val referenceExterne: String? = null,
    val plateforme: String? = null,
    val montant: JsonElement? = null,
    val devise: String? = null,
    val dateCreation: String? = null,
    val dateConfirmation: String? = null,
)

@Serializable
data class RenouvelerRequestDto(
    val modePaiement: String, // fcfa | dsi
)

@Serializable
data class RenouvelerResponseDto(
    val status: String? = null,
    val paymentUrl: String? = null,
    val message: String? = null,
    val erreur: String? = null,
)


@Serializable
data class UtiliserCodeRequestDto(val code: String)

@Serializable
data class UtiliserCodeResponseDto(
    val status: String? = null,
    val montantDsi: JsonElement? = null,
    val message: String? = null,
    val erreur: String? = null,
)

@Serializable
data class RetraitHistoriqueDto(
    val idRetrait: String? = null,
    val evenements: List<RetraitEvenementDto>? = null,
    val erreur: String? = null,
)

@Serializable
data class RetraitEvenementDto(
    val date: String? = null,
    val champ: String? = null,
    val ancienneValeur: String? = null,
    val nouvelleValeur: String? = null,
)
