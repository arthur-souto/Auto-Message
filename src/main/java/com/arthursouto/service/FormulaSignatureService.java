package com.arthursouto.service;

import com.arthursouto.config.FormulaSigningCache;
import com.arthursouto.domain.Formula;
import com.arthursouto.domain.User;
import com.arthursouto.dto.FormulaSignatureFinishRequest;
import com.arthursouto.dto.FormulaSignaturePrepareRequest;
import com.arthursouto.dto.FormulaSignaturePrepareResponse;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ForbiddenException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.FormulaRepository;
import com.arthursouto.repository.UserRepository;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Client-side ICP-Brasil (PAdES) signing of a formula's PDF laudo: the doctor's private key
 * never leaves the browser. This service only prepares the bytes to sign and, later, embeds the
 * signature the browser produced — see {@code SignFormulaModal.tsx} on the frontend for the
 * matching client-side half of this flow.
 */
@Service
@RequiredArgsConstructor
public class FormulaSignatureService {

    private static final Duration SESSION_TTL = Duration.ofMinutes(10);

    private final FormulaRepository formulaRepository;
    private final UserRepository userRepository;
    private final FormulaPdfService formulaPdfService;
    private final FormulaSigningCache formulaSigningCache;
    private final PAdESService padesService;
    private final CommonCertificateVerifier certificateVerifier;

    @Transactional(readOnly = true)
    public FormulaSignaturePrepareResponse prepare(UUID formulaId, FormulaSignaturePrepareRequest request) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);

        CertificateFactory certificateFactory = certificateFactory();
        X509Certificate certificate = parseCertificate(certificateFactory, request.certificateBase64());
        List<X509Certificate> chain = request.certificateChainBase64().stream()
                .map(der -> parseCertificate(certificateFactory, der))
                .toList();

        Date signingDate = new Date();

        // The certificate subject/date must be known before rendering, so the visible "assinado
        // digitalmente" text baked into the PDF always matches what actually gets signed below —
        // generatePdfForSigning() re-validates ownership internally (via FormulaService.findById),
        // a formula belonging to another user throws ResourceNotFoundException here too.
        byte[] unsignedPdf = formulaPdfService.generatePdfForSigning(
                formulaId, certificate.getSubjectX500Principal().getName(), signingDate.toInstant());

        PAdESSignatureParameters params = buildParameters(certificate, chain, signingDate);

        DSSDocument toSignDocument = new InMemoryDocument(unsignedPdf, "formula-" + formulaId + ".pdf", MimeTypeEnum.PDF);
        ToBeSigned dataToSign = padesService.getDataToSign(toSignDocument, params);

        FormulaSigningCache.SigningSession session = new FormulaSigningCache.SigningSession(
                formulaId,
                user.getId(),
                unsignedPdf,
                encode(certificate),
                chain.stream().map(this::encode).toList(),
                signingDate.getTime()
        );

        String sessionId = formulaSigningCache.prepare(session, SESSION_TTL);

        return new FormulaSignaturePrepareResponse(sessionId, Base64.getEncoder().encodeToString(dataToSign.getBytes()));
    }

    @Transactional
    public byte[] finish(UUID formulaId, FormulaSignatureFinishRequest request) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        Formula formula = formulaRepository.findByIdAndUserId(formulaId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found"));

        FormulaSigningCache.SigningSession session = formulaSigningCache.consume(request.sessionId())
                .orElseThrow(() -> new BadRequestException("Signing session expired or already used"));

        if (!session.formulaId().equals(formulaId) || !session.userId().equals(user.getId())) {
            throw new ForbiddenException("Signing session does not match this formula");
        }

        CertificateFactory certificateFactory = certificateFactory();
        X509Certificate certificate = parseCertificateDer(certificateFactory, session.signingCertificateDer());
        List<X509Certificate> chain = session.certificateChainDer().stream()
                .map(der -> parseCertificateDer(certificateFactory, der))
                .toList();

        // Reuse the exact date and PDF bytes captured in prepare() — recomputing either one
        // (e.g. a fresh `new Date()`, or re-rendering the PDF) would produce a different digest
        // than the one the browser actually signed, and the signature would not verify.
        Date signingDate = new Date(session.signingDateEpochMillis());
        PAdESSignatureParameters params = buildParameters(certificate, chain, signingDate);

        DSSDocument toSignDocument = new InMemoryDocument(session.unsignedPdf(), "formula-" + formulaId + ".pdf", MimeTypeEnum.PDF);

        byte[] signatureBytes;
        try {
            signatureBytes = Base64.getDecoder().decode(request.signatureValueBase64());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid signature value");
        }
        SignatureValue signatureValue = new SignatureValue(SignatureAlgorithm.RSA_SHA256, signatureBytes);

        DSSDocument signedDocument;
        try {
            signedDocument = padesService.signDocument(toSignDocument, params, signatureValue);
        } catch (Exception e) {
            throw new BadRequestException("Failed to embed signature: " + e.getMessage());
        }

        byte[] signedPdf = toBytes(signedDocument);
        assertSignatureIntact(signedDocument);

        formula.setSignedPdf(signedPdf);
        formula.setSignedAt(Instant.now());
        formula.setSignedByCertificateSubject(certificate.getSubjectX500Principal().getName());
        formulaRepository.save(formula);

        return signedPdf;
    }

    @Transactional(readOnly = true)
    public byte[] getSignedPdf(UUID formulaId) {
        byte[] signedPdf = findOwnedFormula(formulaId).getSignedPdf();
        if (signedPdf == null) {
            throw new ResourceNotFoundException("Formula is not signed");
        }
        return signedPdf;
    }

    /**
     * Used by the plain "download PDF" action: if the formula has already been signed, that
     * signed PDF is the authoritative current document and must be served — never silently
     * regenerate a fresh unsigned draft for an already-signed formula (it would wrongly print
     * "Documento não assinado digitalmente" even though a valid signature exists).
     */
    @Transactional(readOnly = true)
    public Optional<byte[]> findSignedPdf(UUID formulaId) {
        return Optional.ofNullable(findOwnedFormula(formulaId).getSignedPdf());
    }

    private Formula findOwnedFormula(UUID formulaId) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        return formulaRepository.findByIdAndUserId(formulaId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found"));
    }

    private PAdESSignatureParameters buildParameters(X509Certificate certificate, List<X509Certificate> chain, Date signingDate) {
        PAdESSignatureParameters params = new PAdESSignatureParameters();
        params.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        params.setDigestAlgorithm(DigestAlgorithm.SHA256);
        params.setSigningCertificate(new CertificateToken(certificate));
        params.setCertificateChain(chain.stream().map(CertificateToken::new).toList());
        params.bLevel().setSigningDate(signingDate);
        return params;
    }

    /**
     * DSS itself can produce a structurally-fine-looking PDF from a bad SignatureValue — this
     * re-runs DSS's own validator on the result and refuses to persist anything that doesn't
     * cryptographically check out. Uses the same bare (no OCSP/CRL) verifier as {@link #certificateVerifier},
     * so this stays fast/offline — it's checking the signature math, not the trust chain.
     */
    private void assertSignatureIntact(DSSDocument signedDocument) {
        SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(signedDocument);
        validator.setCertificateVerifier(certificateVerifier);

        Reports reports = validator.validateDocument();
        List<SignatureWrapper> signatures = reports.getDiagnosticData().getSignatures();

        boolean allIntact = !signatures.isEmpty() && signatures.stream().allMatch(SignatureWrapper::isSignatureIntact);
        if (!allIntact) {
            throw new BadRequestException("Signature verification failed");
        }
    }

    private CertificateFactory certificateFactory() {
        try {
            return CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("X.509 CertificateFactory not available", e);
        }
    }

    private X509Certificate parseCertificate(CertificateFactory factory, String base64Der) {
        byte[] der;
        try {
            der = Base64.getDecoder().decode(base64Der);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid certificate encoding");
        }
        return parseCertificateDer(factory, der);
    }

    private X509Certificate parseCertificateDer(CertificateFactory factory, byte[] der) {
        try {
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der));
        } catch (CertificateException e) {
            throw new BadRequestException("Invalid certificate: " + e.getMessage());
        }
    }

    private byte[] encode(X509Certificate certificate) {
        try {
            return certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new BadRequestException("Invalid certificate: " + e.getMessage());
        }
    }

    private byte[] toBytes(DSSDocument document) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.writeTo(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read signed document", e);
        }
    }
}
