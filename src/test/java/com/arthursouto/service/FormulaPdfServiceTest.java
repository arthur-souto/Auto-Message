package com.arthursouto.service;

import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.dto.FormulaItemResponse;
import com.arthursouto.dto.FormulaResponse;
import com.arthursouto.dto.IncompatibilityWarningResponse;
import com.arthursouto.rules.ConcentrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FormulaPdfServiceTest {

    private FormulaPdfService formulaPdfService;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new Java8TimeDialect());

        formulaPdfService = new FormulaPdfService(null, templateEngine);
    }

    @Test
    void rendersAValidPdfDocumentWithItemsWarningsAndDoctor() {
        FormulaItemResponse withinRange = new FormulaItemResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Melatonina", "AT0001",
                BigDecimal.valueOf(3), "mg", BigDecimal.valueOf(2), ConcentrationStatus.WITHIN_RANGE
        );
        FormulaItemResponse noStatus = new FormulaItemResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Cafeína Anidra", "AT0002",
                BigDecimal.TEN, "mg", null, null
        );

        IncompatibilityWarningResponse warning = new IncompatibilityWarningResponse(
                withinRange.assetId(), withinRange.assetName(),
                noStatus.assetId(), noStatus.assetName(),
                "Reduz a absorção quando combinados"
        );

        DoctorResponse doctor = new DoctorResponse(
                UUID.randomUUID(), "Dra. Maria Souza", "CRM/SP 123456", "Endocrinologia",
                Instant.now(), Instant.now()
        );

        FormulaResponse formula = new FormulaResponse(
                UUID.randomUUID(),
                "Fórmula Energia e Foco",
                "Cápsula manipulada para fadiga",
                doctor,
                List.of(withinRange, noStatus),
                List.of(warning),
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        byte[] pdf = formulaPdfService.render(formula, null);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void rendersPdfWithDigitalSignatureBlockWhenSigning() {
        FormulaItemResponse item = new FormulaItemResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Vitamina C", "AT0006",
                BigDecimal.valueOf(500), "mg", null, null
        );

        FormulaResponse formula = new FormulaResponse(
                UUID.randomUUID(), "Fórmula assinada", null, null,
                List.of(item), List.of(), null, null, Instant.now(), Instant.now()
        );

        FormulaPdfService.DigitalSignatureInfo signatureInfo =
                new FormulaPdfService.DigitalSignatureInfo("CN=Pierre de Fermat,O=Lacuna Software,C=BR", Instant.now());

        byte[] pdf = formulaPdfService.render(formula, signatureInfo);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void rendersPdfWithoutDoctorOrWarnings() {
        FormulaItemResponse item = new FormulaItemResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Vitamina C", "AT0006",
                BigDecimal.valueOf(500), "mg", null, null
        );

        FormulaResponse formula = new FormulaResponse(
                UUID.randomUUID(), "Fórmula simples", null, null,
                List.of(item), List.of(), null, null, Instant.now(), Instant.now()
        );

        byte[] pdf = formulaPdfService.render(formula, null);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
