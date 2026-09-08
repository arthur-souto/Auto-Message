package com.arthursouto.service;

import com.arthursouto.exception.BadRequestException;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
class AssetImportXlsxParser {

    static List<RawAssetRow> parse(MultipartFile file) {
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new BadRequestException("Could not read XLSX file: " + e.getMessage());
        }

        try {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new BadRequestException("Missing required column(s): code, name");
            }

            Map<Integer, String> headerByIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                headerByIndex.put(cell.getColumnIndex(), cellToString(cell).trim().toLowerCase());
            }
            if (!headerByIndex.containsValue("code") || !headerByIndex.containsValue("name")) {
                throw new BadRequestException("Missing required column(s): code, name");
            }

            List<RawAssetRow> rows = new ArrayList<>();
            int firstDataRow = headerRow.getRowNum() + 1;
            for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row)) continue;

                Map<String, String> cells = new HashMap<>();
                for (Map.Entry<Integer, String> entry : headerByIndex.entrySet()) {
                    Cell cell = row.getCell(entry.getKey());
                    cells.put(entry.getValue(), cell == null ? null : cellToString(cell));
                }
                rows.add(RawAssetRow.from(rowIndex + 1, cells));
            }
            return rows;
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK && !cellToString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String cellToString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                yield value == Math.floor(value) && !Double.isInfinite(value)
                        ? String.valueOf((long) value)
                        : String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
