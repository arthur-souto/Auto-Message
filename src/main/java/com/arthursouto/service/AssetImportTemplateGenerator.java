package com.arthursouto.service;

import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@UtilityClass
class AssetImportTemplateGenerator {

    static byte[] generate(String format) {
        return "xlsx".equalsIgnoreCase(format) ? generateXlsx() : generateCsv();
    }

    private static byte[] generateCsv() {
        try (var out = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             var printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(RawAssetRow.COLUMNS);
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] generateXlsx() {
        try (var workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("assets");
            Row header = sheet.createRow(0);
            for (int i = 0; i < RawAssetRow.COLUMNS.size(); i++) {
                header.createCell(i).setCellValue(RawAssetRow.COLUMNS.get(i));
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
