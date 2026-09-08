package com.arthursouto.service;

import com.arthursouto.exception.BadRequestException;
import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
class AssetImportCsvParser {

    static List<RawAssetRow> parse(MultipartFile file) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            List<String> headerNames = parser.getHeaderNames();
            validateHeaders(headerNames);

            List<RawAssetRow> rows = new ArrayList<>();
            int rowNumber = 2;
            for (CSVRecord record : parser) {
                Map<String, String> cells = new HashMap<>();
                for (String header : headerNames) {
                    cells.put(header.trim().toLowerCase(), record.isSet(header) ? record.get(header) : null);
                }
                rows.add(RawAssetRow.from(rowNumber, cells));
                rowNumber++;
            }
            return rows;
        } catch (IOException e) {
            throw new BadRequestException("Could not read CSV file: " + e.getMessage());
        }
    }

    private static void validateHeaders(List<String> headerNames) {
        List<String> normalized = headerNames.stream().map(h -> h.trim().toLowerCase()).toList();
        if (!normalized.contains("code") || !normalized.contains("name")) {
            throw new BadRequestException("Missing required column(s): code, name");
        }
    }
}
