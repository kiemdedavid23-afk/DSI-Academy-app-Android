package app.dsiacademy.academy.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkPolicyTest {

    private val siteUrl = "https://dsiacademy.com"
    private val externalHosts = setOf("facebook.com", "wa.me", "tiktok.com")
    private val policy = LinkPolicy(siteUrl, externalHosts)

    // --- a. URL vide ou illisible → Block ---

    @Test
    fun decide_urlVide_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide(""))
        assertEquals(LinkDecision.Block, policy.decide("   "))
    }

    @Test
    fun decide_urlIllisible_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("not a url at all :::"))
        assertEquals(LinkDecision.Block, policy.decide("https://[invalid"))
    }

    // --- b. Schémas interdits → Block ---

    @Test
    fun decide_schemaJavascript_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("javascript:alert(1)"))
        assertEquals(LinkDecision.Block, policy.decide("JAVASCRIPT:void(0)"))
    }

    @Test
    fun decide_schemaFile_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("file:///etc/passwd"))
    }

    @Test
    fun decide_schemaData_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("data:text/html,hello"))
    }

    @Test
    fun decide_schemaContent_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("content://media/external"))
    }

    @Test
    fun decide_schemaBlob_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("blob:https://example.com/uuid"))
    }

    @Test
    fun decide_schemaAbout_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("about:blank"))
    }

    @Test
    fun decide_schemaHttp_retourneBlock() {
        assertEquals(LinkDecision.Block, policy.decide("http://dsiacademy.com/inscription"))
    }

    // --- c. Schéma autre que https → OpenExternal ---

    @Test
    fun decide_schemaTel_retourneOpenExternal() {
        val url = "tel:+22678627530"
        val result = policy.decide(url)
        assertTrue(result is LinkDecision.OpenExternal)
        assertEquals(url, (result as LinkDecision.OpenExternal).url)
    }

    @Test
    fun decide_schemaMailto_retourneOpenExternal() {
        val url = "mailto:contact@dsiacademy.com"
        assertTrue(policy.decide(url) is LinkDecision.OpenExternal)
    }

    @Test
    fun decide_schemaWhatsapp_retourneOpenExternal() {
        val url = "whatsapp://send?phone=22678627530"
        assertTrue(policy.decide(url) is LinkDecision.OpenExternal)
    }

    @Test
    fun decide_schemaIntent_retourneOpenExternal() {
        assertTrue(policy.decide("intent://scan/#Intent;scheme=zxing;end") is LinkDecision.OpenExternal)
    }

    // --- d. Site non configuré → Block ---

    @Test
    fun decide_siteNonConfigure_retourneBlock() {
        val emptyPolicy = LinkPolicy("", externalHosts)
        assertFalse(emptyPolicy.isConfigured)
        assertEquals(LinkDecision.Block, emptyPolicy.decide("https://dsiacademy.com/inscription"))
    }

    @Test
    fun startUrl_siteNonConfigure_retourneNull() {
        val emptyPolicy = LinkPolicy("", externalHosts)
        assertNull(emptyPolicy.startUrl("/inscription"))
        assertNull(emptyPolicy.startUrl("/inscription", mapOf("ref" to "ABC")))
    }

    // --- e. PaymentReturn ---

    @Test
    fun decide_paiementRetour_statusSuccess_retournePaymentReturnSuccess() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=success&token=abc")
        assertTrue(result is LinkDecision.PaymentReturn)
        val pr = result as LinkDecision.PaymentReturn
        assertEquals(PaymentReturnStatus.Success, pr.status)
        assertEquals("abc", pr.trackingToken)
    }

    @Test
    fun decide_paiementRetour_statusOk_retourneSuccess() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=ok")
        assertEquals(PaymentReturnStatus.Success, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_statusPaid_retourneSuccess() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=paid")
        assertEquals(PaymentReturnStatus.Success, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_statusFailed_retourneFailed() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=failed")
        assertEquals(PaymentReturnStatus.Failed, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_statusError_retourneFailed() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=error")
        assertEquals(PaymentReturnStatus.Failed, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_statusCancel_retourneFailed() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=cancel")
        assertEquals(PaymentReturnStatus.Failed, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_statusAbsent_retourneUnknown() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour")
        assertEquals(PaymentReturnStatus.Unknown, (result as LinkDecision.PaymentReturn).status)
        assertNull(result.trackingToken)
    }

    @Test
    fun decide_paiementRetour_statusInconnu_retourneUnknown() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=pending")
        assertEquals(PaymentReturnStatus.Unknown, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_paramEtat_estLu() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?etat=success")
        assertEquals(PaymentReturnStatus.Success, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_tokenSousPaymentTrackingToken() {
        val result = policy.decide(
            "https://dsiacademy.com/paiement-retour?status=ok&paymentTrackingToken=tok123"
        )
        assertEquals("tok123", (result as LinkDecision.PaymentReturn).trackingToken)
    }

    @Test
    fun decide_paiementRetour_tokenVide_retourneNull() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour?status=ok&token=")
        assertNull((result as LinkDecision.PaymentReturn).trackingToken)
    }

    @Test
    fun decide_paiementRetour_barreFinaleToleree() {
        val result = policy.decide("https://dsiacademy.com/paiement-retour/?status=success&token=x")
        assertTrue(result is LinkDecision.PaymentReturn)
        assertEquals(PaymentReturnStatus.Success, (result as LinkDecision.PaymentReturn).status)
    }

    @Test
    fun decide_paiementRetour_wwwIgnore() {
        val result = policy.decide("https://www.dsiacademy.com/paiement-retour?status=ok&token=t")
        assertTrue(result is LinkDecision.PaymentReturn)
        assertEquals("t", (result as LinkDecision.PaymentReturn).trackingToken)
    }

    @Test
    fun decide_paiementRetour_casseIgnoree() {
        val result = policy.decide("HTTPS://DSIACADEMY.COM/paiement-retour?Status=SUCCESS&Token=ABC")
        assertTrue(result is LinkDecision.PaymentReturn)
        val pr = result as LinkDecision.PaymentReturn
        assertEquals(PaymentReturnStatus.Success, pr.status)
        assertEquals("ABC", pr.trackingToken)
    }

    // --- f. Hôte externe → OpenExternal ---

    @Test
    fun decide_hoteExterneFacebook_retourneOpenExternal() {
        val url = "https://www.facebook.com/share/1GVL3d7duA/"
        val result = policy.decide(url)
        assertTrue(result is LinkDecision.OpenExternal)
        assertEquals(url, (result as LinkDecision.OpenExternal).url)
    }

    @Test
    fun decide_hoteExterneTiktok_retourneOpenExternal() {
        assertTrue(
            policy.decide("https://www.tiktok.com/@dsi169") is LinkDecision.OpenExternal
        )
    }

    @Test
    fun decide_sousDomaineExterne_retourneOpenExternal() {
        val url = "https://m.facebook.com/something"
        assertTrue(policy.decide(url) is LinkDecision.OpenExternal)
    }

    @Test
    fun decide_hoteExterneWaMe_retourneOpenExternal() {
        assertTrue(policy.decide("https://wa.me/22678627530") is LinkDecision.OpenExternal)
    }

    // --- g. Autre https → Allow ---

    @Test
    fun decide_pageSite_retourneAllow() {
        assertEquals(LinkDecision.Allow, policy.decide("https://dsiacademy.com/inscription"))
        assertEquals(LinkDecision.Allow, policy.decide("https://www.dsiacademy.com/connexion"))
    }

    @Test
    fun decide_passerellePaiement_retourneAllow() {
        // Passerelle de paiement atteinte depuis le site (autre hôte https non listé)
        assertEquals(
            LinkDecision.Allow,
            policy.decide("https://checkout.chariow.com/pay/xyz")
        )
    }

    @Test
    fun decide_casseHttpsEtHote_retourneAllow() {
        assertEquals(
            LinkDecision.Allow,
            policy.decide("HTTPS://DSIACADEMY.COM/inscription")
        )
    }

    // --- startUrl ---

    @Test
    fun startUrl_sansQuery_construitUrlCorrecte() {
        assertEquals(
            "https://dsiacademy.com/inscription",
            policy.startUrl("/inscription")
        )
    }

    @Test
    fun startUrl_avecQuery_encodeLesValeurs() {
        val url = policy.startUrl("/inscription", mapOf("ref" to "ABC 123", "x" to "a=b"))
        assertEquals(
            "https://dsiacademy.com/inscription?ref=ABC+123&x=a%3Db",
            url
        )
    }

    @Test
    fun startUrl_pathSansSlashInitial_ajouteSlash() {
        assertEquals(
            "https://dsiacademy.com/inscription",
            policy.startUrl("inscription")
        )
    }

    @Test
    fun isConfigured_vraiQuandSiteUrlRenseigne() {
        assertTrue(policy.isConfigured)
    }
}
