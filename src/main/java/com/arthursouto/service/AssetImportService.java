package com.arthursouto.service;

import com.arthursouto.dto.AssetImportResponse;
import com.arthursouto.dto.AssetImportRowError;
import com.arthursouto.dto.AssetRequest;
import com.arthursouto.exception.AssetImportRowException;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetImportService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("csv", "xlsx");

    private final UserRepository userRepository;
    private final Validator validator;
    private final AssetImportRowWriter assetImportRowWriter;

    public AssetImportResponse importAssets(MultipartFile file) {
        AuthenticatedUser.isAccountVerified(userRepository);

        String extension = validateFile(file);
        List<RawAssetRow> rows = "csv".equals(extension)
                ? AssetImportCsvParser.parse(file)
                : AssetImportXlsxParser.parse(file);

        int created = 0;
        int updated = 0;
        List<AssetImportRowError> errors = new ArrayList<>();

        for (RawAssetRow row : rows) {
            if (row.hasParseError()) {
                errors.add(new AssetImportRowError(row.rowNumber(), row.rawCode(), row.parseError()));
                continue;
            }

            String validationError = validate(row.assetRequest());
            if (validationError != null) {
                errors.add(new AssetImportRowError(row.rowNumber(), row.rawCode(), validationError));
                continue;
            }

            try {
                AssetImportRowWriter.Result result = assetImportRowWriter.upsertRow(row.assetRequest());
                if (result == AssetImportRowWriter.Result.CREATED) {
                    created++;
                } else {
                    updated++;
                }
            } catch (AssetImportRowException e) {
                errors.add(new AssetImportRowError(row.rowNumber(), row.rawCode(), e.getMessage()));
            }
        }

        return AssetImportResponse.of(rows.size(), created, updated, errors);
    }

    public byte[] generateTemplate(String format) {
        AuthenticatedUser.isAccountVerified(userRepository);
        return AssetImportTemplateGenerator.generate(format);
    }

    private String validate(AssetRequest request) {
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Expected .csv or .xlsx");
        }

        return extension;
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
