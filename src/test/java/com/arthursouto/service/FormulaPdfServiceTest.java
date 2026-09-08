package com.arthursouto.service;

import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.dto.FormulaItemResponse;
import com.arthursouto.dto.FormulaResponse;
import com.arthursouto.dto.IncompatibilityWarningResponse;
import com.arthursouto.dto.PatientResponse;
import com.arthursouto.dto.PharmacyProfileResponse;
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

        formulaPdfService = new FormulaPdfService(null, null, templateEngine);
    }

    @Test
    void rendersAValidPdfDocumentWithItemsWarningsDoctorPatientAndPharmacyProfile() {
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

        PatientResponse patient = new PatientResponse(
                UUID.randomUUID(), "João Pereira", "123.456.789-00", null, "(11) 99999-0000", null, null, null,
                Instant.now(), Instant.now()
        );

        PharmacyProfileResponse pharmacyProfile = new PharmacyProfileResponse(
                UUID.randomUUID(), "Farmácia Exemplo", "Rua das Flores, 123", "(11) 3333-4444",
                "contato@farmaciaexemplo.com.br", "Dra. Maria Souza", "037.261.499-02", "CRF 115070/SP",
                Instant.now(), Instant.now()
        );

        FormulaResponse formula = new FormulaResponse(
                UUID.randomUUID(),
                "Fórmula Energia e Foco",
                "Cápsula manipulada para fadiga",
                doctor,
                patient,
                "1 dose após o café da manhã",
                "30 doses",
                List.of(withinRange, noStatus),
                List.of(warning),
                Instant.now(),
                Instant.now()
        );

        byte[] pdf = formulaPdfService.render(formula, pharmacyProfile);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void rendersPdfWithoutDoctorPatientOrPharmacyProfile() {
        FormulaItemResponse item = new FormulaItemResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Vitamina C", "AT0006",
                BigDecimal.valueOf(500), "mg", null, null
        );

        FormulaResponse formula = new FormulaResponse(
                UUID.randomUUID(), "Fórmula simples", null, null, null, null, null,
                List.of(item), List.of(), Instant.now(), Instant.now()
        );

        byte[] pdf = formulaPdfService.render(formula, null);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
