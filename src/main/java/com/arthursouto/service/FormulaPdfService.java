package com.arthursouto.service;

import com.arthursouto.dto.FormulaResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaPdfService {

    private final FormulaService formulaService;
    private final SpringTemplateEngine templateEngine;

    /** Signature block shown printed on the "assinado por CN=..." certificate line in the PDF. */
    public record DigitalSignatureInfo(String certificateSubject, Instant signingDate) {
    }

    public byte[] generatePdf(UUID formulaId) {
        FormulaResponse formula = formulaService.findById(formulaId);
        return render(formula, null);
    }

    /**
     * Renders the exact bytes that get cryptographically signed — the certificate subject and
     * signing date must be known and fixed *before* rendering, so the "assinado digitalmente"
     * text printed in the PDF and the real ICP-Brasil signature always agree with each other.
     */
    public byte[] generatePdfForSigning(UUID formulaId, String certificateSubject, Instant signingDate) {
        FormulaResponse formula = formulaService.findById(formulaId);
        return render(formula, new DigitalSignatureInfo(certificateSubject, signingDate));
    }

    byte[] render(FormulaResponse formula, DigitalSignatureInfo digitalSignature) {
        Context context = new Context();
        context.setVariable("formula", formula);
        context.setVariable("generatedAt", Instant.now());
        context.setVariable("digitalSignature", digitalSignature);

        log.info("Generating PDF for formula ID: {}", formula.id());

        String html = templateEngine.process("pdf/formula-laudo", context);

        log.info("HTML content generated for formula ID: {}. Length: {}", formula.id(), html.length());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
        } catch (Exception e) {
            log.error("Error generating PDF for formula ID: {}", formula.id(), e);
            throw new IllegalStateException("Failed to generate formula PDF", e);
        }

        byte [] pdfBytes = out.toByteArray();
        log.info("PDF generated for formula ID: {}. Size: {} bytes", formula.id(), pdfBytes.length);
        return pdfBytes;
    }
}
