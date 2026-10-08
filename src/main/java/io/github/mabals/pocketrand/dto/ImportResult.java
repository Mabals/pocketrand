package io.github.mabals.pocketrand.dto;

import java.util.List;

public record ImportResult(int imported, int skipped, int duplicates, List<ImportError> errors) {

    public record ImportError(int line, String reason) {
    }
}