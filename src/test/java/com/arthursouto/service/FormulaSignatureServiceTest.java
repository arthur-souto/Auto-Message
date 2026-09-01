package com.arthursouto.service;

import com.arthursouto.config.FormulaSigningCache;
import com.arthursouto.domain.Formula;
import com.arthursouto.domain.User;
import com.arthursouto.dto.FormulaSignatureFinishRequest;
import com.arthursouto.dto.FormulaSignaturePrepareRequest;
import com.arthursouto.dto.FormulaSignaturePrepareResponse;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ForbiddenException;
import com.arthursouto.factory.FormulaFactory;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.FormulaRepository;
import com.arthursouto.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Uses the real DSS {@link PAdESService} (not mocked) plus a real Lacuna test certificate, so
 * this exercises the actual cryptographic round-trip end to end — the same thing the disposable
 * spike proved manually, now as a repeatable test. Only the repository/Redis layers are mocked.
 */
@ExtendWith(MockitoExtension.class)
class FormulaSignatureServiceTest {

    @Mock
    private FormulaRepository formulaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FormulaPdfService formulaPdfService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final Map<String, String> fakeRedis = new HashMap<>();

    private FormulaSignatureService formulaSignatureService;
    private X509Certificate certificate;
    private PrivateKey privateKey;
    private byte[] unsignedPdf;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().doAnswer(invocation -> {
            fakeRedis.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        lenient().when(valueOperations.getAndDelete(anyString()))
                .thenAnswer(invocation -> fakeRedis.remove((String) invocation.getArgument(0)));

        FormulaSigningCache formulaSigningCache = new FormulaSigningCache(redisTemplate, new ObjectMapper());
        CommonCertificateVerifier certificateVerifier = certificateVerifierWithoutAia();
        PAdESService padesService = new PAdESService(certificateVerifier);

        formulaSignatureService = new FormulaSignatureService(
                formulaRepository, userRepository, formulaPdfService, formulaSigningCache, padesService, certificateVerifier);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = getClass().getResourceAsStream("/certs/lacuna-test-cert.pfx")) {
            keyStore.load(in, "1234".toCharArray());
        }
        String alias = keyStore.aliases().nextElement();
        certificate = (X509Certificate) keyStore.getCertificate(alias);
        privateKey = (PrivateKey) keyStore.getKey(alias, "1234".toCharArray());

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            unsignedPdf = out.toByteArray();
        }
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        fakeRedis.clear();
    }

    private User authenticateAsVerifiedUser() {
        User user = UserFactory.userBuilder().isVerified(true).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    /** Mirrors DigitalSignatureConfig's bean: the plain constructor auto-wires a live AIA source. */
    private CommonCertificateVerifier certificateVerifierWithoutAia() {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setAIASource(null);
        return certificateVerifier;
    }

    private String certToBase64() {
        try {
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Mirrors exactly what node-forge does in the browser: SHA-256 digest + PKCS#1 v1.5 RSA sign. */
    private byte[] signWithTestCertificate(byte[] dataToSign) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(dataToSign);
        return signature.sign();
    }

    @Test
    void prepareAndFinishProduceAnIntactPadesSignature() throws Exception {
        User user = authenticateAsVerifiedUser();
        Formula formula = FormulaFactory.formula(user);
        when(formulaPdfService.generatePdfForSigning(eq(formula.getId()), anyString(), any(Instant.class)))
                .thenReturn(unsignedPdf);
        when(formulaRepository.findByIdAndUserId(formula.getId(), user.getId()))
                .thenReturn(Optional.of(formula));

        FormulaSignaturePrepareResponse prepareResponse = formulaSignatureService.prepare(
                formula.getId(),
                new FormulaSignaturePrepareRequest(certToBase64(), List.of(certToBase64())));

        assertThat(prepareResponse.sessionId()).isNotBlank();

        byte[] dataToSign = Base64.getDecoder().decode(prepareResponse.dataToSignBase64());
        byte[] signatureBytes = signWithTestCertificate(dataToSign);

        byte[] signedPdf = formulaSignatureService.finish(
                formula.getId(),
                new FormulaSignatureFinishRequest(prepareResponse.sessionId(), Base64.getEncoder().encodeToString(signatureBytes)));

        assertThat(signedPdf).isNotEmpty();
        assertThat(formula.getSignedPdf()).isEqualTo(signedPdf);
        assertThat(formula.getSignedAt()).isNotNull();
        assertThat(formula.getSignedByCertificateSubject()).contains("Pierre de Fermat");

        DSSDocument document = new InMemoryDocument(signedPdf, "signed.pdf", MimeTypeEnum.PDF);
        SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(document);
        validator.setCertificateVerifier(certificateVerifierWithoutAia());
        Reports reports = validator.validateDocument();
        List<SignatureWrapper> signatures = reports.getDiagnosticData().getSignatures();

        assertThat(signatures).hasSize(1);
        assertThat(signatures.get(0).isSignatureIntact()).isTrue();
        assertThat(signatures.get(0).getSignatureFormat().toString()).contains("PAdES");
    }

    @Test
    void findSignedPdfReturnsThePersistedSignedPdfWhenPresent() {
        User user = authenticateAsVerifiedUser();
        Formula formula = FormulaFactory.formula(user);
        formula.setSignedPdf(unsignedPdf);
        when(formulaRepository.findByIdAndUserId(formula.getId(), user.getId()))
                .thenReturn(Optional.of(formula));

        assertThat(formulaSignatureService.findSignedPdf(formula.getId())).contains(unsignedPdf);
    }

    @Test
    void findSignedPdfReturnsEmptyWhenFormulaHasNotBeenSigned() {
        User user = authenticateAsVerifiedUser();
        Formula formula = FormulaFactory.formula(user);
        when(formulaRepository.findByIdAndUserId(formula.getId(), user.getId()))
                .thenReturn(Optional.of(formula));

        assertThat(formulaSignatureService.findSignedPdf(formula.getId())).isEmpty();
    }

    @Test
    void finishRejectsAnExpiredOrAlreadyUsedSession() {
        User user = authenticateAsVerifiedUser();
        Formula formula = FormulaFactory.formula(user);
        when(formulaRepository.findByIdAndUserId(formula.getId(), user.getId()))
                .thenReturn(Optional.of(formula));

        assertThatThrownBy(() -> formulaSignatureService.finish(
                formula.getId(), new FormulaSignatureFinishRequest("unknown-session", "not-relevant")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void finishRejectsASessionThatDoesNotBelongToTheRequestedFormula() throws Exception {
        User user = authenticateAsVerifiedUser();
        Formula formula = FormulaFactory.formula(user);
        Formula otherFormula = FormulaFactory.formula(user);
        when(formulaPdfService.generatePdfForSigning(eq(formula.getId()), anyString(), any(Instant.class)))
                .thenReturn(unsignedPdf);
        when(formulaRepository.findByIdAndUserId(otherFormula.getId(), user.getId()))
                .thenReturn(Optional.of(otherFormula));

        FormulaSignaturePrepareResponse prepareResponse = formulaSignatureService.prepare(
                formula.getId(),
                new FormulaSignaturePrepareRequest(certToBase64(), List.of(certToBase64())));

        assertThatThrownBy(() -> formulaSignatureService.finish(
                otherFormula.getId(),
                new FormulaSignatureFinishRequest(prepareResponse.sessionId(), "irrelevant")))
                .isInstanceOf(ForbiddenException.class);
    }
}
