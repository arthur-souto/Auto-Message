package com.arthursouto.service;

import com.arthursouto.dto.AssetImportResponse;
import com.arthursouto.dto.AssetRequest;
import com.arthursouto.exception.AssetImportRowException;
import com.arthursouto.exception.BadRequestException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetImportServiceTest {

    @Mock
    private AssetImportRowWriter assetImportRowWriter;

    private AssetImportService assetImportService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        assetImportService = new AssetImportService(validator, assetImportRowWriter);
    }

    private MockMultipartFile csv(String filename, String content) {
        return new MockMultipartFile("file", filename, "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    private static ArgumentMatcher<AssetRequest> codeIs(String code) {
        return request -> request != null && request.code().equals(code);
    }

    @Test
    void importAssetsCreatesAndUpdatesRowsByCode() {
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A1"))))
                .thenReturn(AssetImportRowWriter.Result.CREATED);
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A2"))))
                .thenReturn(AssetImportRowWriter.Result.UPDATED);

        String content = """
                code,name
                A1,Asset One
                A2,Asset Two
                """;

        AssetImportResponse response = assetImportService.importAssets(csv("assets.csv", content));

        assertThat(response.totalRows()).isEqualTo(2);
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.updatedCount()).isEqualTo(1);
        assertThat(response.failedCount()).isZero();
        assertThat(response.errors()).isEmpty();
    }

    @Test
    void importAssetsIsolatesRowFailuresAndContinuesProcessingRemainingRows() {
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A1"))))
                .thenReturn(AssetImportRowWriter.Result.CREATED);
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A3"))))
                .thenThrow(new AssetImportRowException("Database constraint violation: boom"));
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A4"))))
                .thenReturn(AssetImportRowWriter.Result.CREATED);

        String content = """
                code,name
                A1,Asset One
                A2,
                A3,Asset Three
                A4,Asset Four
                """;

        AssetImportResponse response = assetImportService.importAssets(csv("assets.csv", content));

        assertThat(response.totalRows()).isEqualTo(4);
        assertThat(response.createdCount()).isEqualTo(2);
        assertThat(response.failedCount()).isEqualTo(2);
        assertThat(response.errors())
                .extracting("rowNumber")
                .containsExactly(3, 4);
        assertThat(response.errors().get(0).reason()).contains("name is required");
        assertThat(response.errors().get(1).reason()).contains("Database constraint violation");

        verify(assetImportRowWriter, never()).upsertRow(argThat(codeIs("A2")));
    }

    @Test
    void importAssetsHandlesMalformedCellsAsRowErrorsWithoutBlockingOtherRows() {
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A1"))))
                .thenReturn(AssetImportRowWriter.Result.CREATED);

        String content = """
                code,name,is_exclusive,concentration_min
                A1,Asset One,true,10
                A2,Asset Two,maybe,
                A3,Asset Three,,abc
                """;

        AssetImportResponse response = assetImportService.importAssets(csv("assets.csv", content));

        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.failedCount()).isEqualTo(2);
        assertThat(response.errors().get(0).reason()).contains("is_exclusive");
        assertThat(response.errors().get(1).reason()).contains("concentration_min");
    }

    @Test
    void importAssetsParsesXlsxEquivalentToCsv() {
        when(assetImportRowWriter.upsertRow(argThat(codeIs("A1"))))
                .thenReturn(AssetImportRowWriter.Result.CREATED);

        MockMultipartFile file = xlsx(new String[][]{
                {"code", "name"},
                {"A1", "Asset One"}
        });

        AssetImportResponse response = assetImportService.importAssets(file);

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.failedCount()).isZero();
    }

    @Test
    void importAssetsRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "assets.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> assetImportService.importAssets(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Uploaded file is empty");

        verifyNoMoreInteractions(assetImportRowWriter);
    }

    @Test
    void importAssetsRejectsUnsupportedExtension() {
        MockMultipartFile file = csv("assets.txt", "code,name\nA1,Asset One\n");

        assertThatThrownBy(() -> assetImportService.importAssets(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported file type. Expected .csv or .xlsx");
    }

    @Test
    void importAssetsRejectsFileMissingRequiredHeaders() {
        MockMultipartFile file = csv("assets.csv", "supplier,category\nAcme,Category A\n");

        assertThatThrownBy(() -> assetImportService.importAssets(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing required column(s): code, name");
    }

    @Test
    void generateTemplateReturnsCsvHeaderRow() {
        byte[] content = assetImportService.generateTemplate("csv");

        assertThat(new String(content, StandardCharsets.UTF_8)).contains("code", "name");
    }

    private MockMultipartFile xlsx(String[][] rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("assets");
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < rows[r].length; c++) {
                    row.createCell(c).setCellValue(rows[r][c]);
                }
            }
            workbook.write(out);
            return new MockMultipartFile(
                    "file",
                    "assets.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
