package com.arthursouto.service;

import com.arthursouto.dto.FormulaResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormulaPdfService {

    private final FormulaService formulaService;
    private final SpringTemplateEngine templateEngine;

    public byte[] generatePdf(UUID formulaId) {
        FormulaResponse formula = formulaService.findById(formulaId);
        return render(formula);
    }

    byte[] render(FormulaResponse formula) {
        Context context = new Context();
        context.setVariable("formula", formula);
        context.setVariable("generatedAt", Instant.now());

        String html = templateEngine.process("pdf/formula-laudo", context);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate formula PDF", e);
        }

        return out.toByteArray();
    }
}
