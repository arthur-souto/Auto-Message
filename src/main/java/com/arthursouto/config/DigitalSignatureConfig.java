package com.arthursouto.config;

import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DigitalSignatureConfig {

    /**
     * PAdES-BASELINE-B only, by design: no revocation checking, so OCSP/CRL are left unset (they
     * default to null and DSS makes no calls). AIA is the one exception worth calling out —
     * {@code new CommonCertificateVerifier()} auto-wires a live {@code DefaultAIASource} that
     * fetches issuer certificates over HTTP (confirmed by decompiling the constructor; not
     * documented anywhere obvious), so it's explicitly disabled here to keep this verifier
     * offline. Do not re-enable AIA/OCSP/CRL without also moving to PAdES-BASELINE-T/LTV — those
     * levels are what actually require this kind of chain/revocation resolution.
     */
    @Bean
    public CommonCertificateVerifier certificateVerifier() {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setAIASource(null);
        return certificateVerifier;
    }

    @Bean
    public PAdESService padesService(CommonCertificateVerifier certificateVerifier) {
        return new PAdESService(certificateVerifier);
    }
}
