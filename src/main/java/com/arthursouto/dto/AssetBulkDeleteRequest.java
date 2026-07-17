package com.arthursouto.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AssetBulkDeleteRequest(
        @NotEmpty List<UUID> ids
) {
}
