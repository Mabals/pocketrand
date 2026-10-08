package io.github.mabals.pocketrand.dto;

import java.util.List;

public record SpendingTips(String month, List<String> tips, boolean aiGenerated) {
}