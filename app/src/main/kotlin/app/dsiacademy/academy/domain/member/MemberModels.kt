package app.dsiacademy.academy.domain.member

data class MemberProfil(
    val prenom: String,
    val nom: String,
    val identifiantDsi: String,
    val niveau: String,
    val codeParrainage: String,
    val statut: String,
    val dateInscription: String?,
    val dateActivation: String?,
    val whatsapp: String?,
    val email: String?,
    val pays: String?,
)

data class MemberSolde(
    val identifiantDsi: String?,
    val soldeDsiActuel: Double,
    val totalDsiGagnes: Double,
    val dateMiseAJour: String?,
)

data class MemberAbonnement(
    val statut: String?,
    val dateDebut: String?,
    val dateFin: String?,
    val formule: String?,
    val modePaiement: String?,
    val renouvellements: Int?,
) {
    /**
     * Règle exacte site (computeSubscription + CONTRATS §2.6) :
     * - dateFin valide et future → active (+ jours, percent)
     * - dateFin valide et passée → expired
     * - sinon → active si statut=="active", sinon none
     */
    val etatAffiche: String
        get() {
            val finMs = parseDateMs(dateFin)
            if (finMs != null) {
                return if (finMs > System.currentTimeMillis()) "active" else "expired"
            }
            return if (statut.equals("active", true)) "active" else "none"
        }

    val joursRestants: Int?
        get() {
            val finMs = parseDateMs(dateFin) ?: return null
            val left = finMs - System.currentTimeMillis()
            if (left <= 0) return 0
            return ((left + 86_400_000 - 1) / 86_400_000).toInt()
        }

    val progressPercent: Int
        get() {
            val finMs = parseDateMs(dateFin) ?: return 0
            val debutMs = parseDateMs(dateDebut)
            val left = finMs - System.currentTimeMillis()
            if (left <= 0) return 0
            if (debutMs == null || finMs <= debutMs) return 100
            val total = finMs - debutMs
            return ((left * 100.0) / total).toInt().coerceIn(0, 100)
        }
}

private fun parseDateMs(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    return try {
        // ISO-8601 partial support
        java.time.Instant.parse(raw).toEpochMilli()
    } catch (_: Exception) {
        try {
            java.time.LocalDate.parse(raw.take(10)).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}

data class MemberParrainage(
    val codeParrainagePersonnel: String,
    val filleulsDirectsTotal: Int,
    val filleulsDirectsActifs: Int,
    val recompensesNiveau1Dsi: Double,
    val recompensesNiveau2Dsi: Double,
    val totalDsiParrainage: Double,
    val historique: List<ParrainageLigne>,
)

data class ParrainageLigne(
    val type: String,
    val montant: Double,
    val identifiantDsiFilleul: String?,
    val date: String?,
)

data class MemberDashboard(
    val profil: MemberProfil,
    val solde: MemberSolde?,
    val abonnement: MemberAbonnement?,
    val retraits: List<RetraitItem> = emptyList(),
)

data class RetraitItem(
    val idRetrait: String,
    val statut: String,
    val moyenPaiement: String?,
    val montantDsiDemande: Double,
    val montantDsiNet: Double,
    val montantFcfa: Double?,
    val dateDemande: String?,
)

data class RetraitsList(
    val total: Int,
    val retraits: List<RetraitItem>,
)

data class DemandeRetraitResult(
    val status: String,
    val idRetrait: String?,
    val message: String?,
    val montantDsiNet: Double?,
    val montantFcfaNet: Double?,
)

data class PaiementItem(
    val idTransaction: String,
    val typePaiement: String?,
    val statut: String,
    val montant: Double,
    val devise: String?,
    val plateforme: String?,
    val dateCreation: String?,
    val dateConfirmation: String?,
)

data class PaiementsPage(
    val paiements: List<PaiementItem>,
    val page: Int,
    val totalPages: Int,
    val total: Int,
)

data class RenouvelerResult(
    val status: String,
    val paymentUrl: String?,
    val message: String?,
)


sealed class GiftCodeResult {
    data class Success(val montantDsi: Double, val message: String?) : GiftCodeResult()
    data class Failure(val reason: String) : GiftCodeResult() // alreadyUsed | invalid | session | empty | unavailable
}

data class RetraitEvenement(
    val date: String?,
    val champ: String?,
    val ancienneValeur: String?,
    val nouvelleValeur: String?,
)
