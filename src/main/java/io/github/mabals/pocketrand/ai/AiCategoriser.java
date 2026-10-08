package io.github.mabals.pocketrand.ai;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.mabals.pocketrand.model.Category;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AiCategoriser {

    private static final Logger log = LoggerFactory.getLogger(AiCategoriser.class);
    private static final int BATCH_SIZE = 50;

    private final GeminiClient gemini;
    private final JsonMapper jsonMapper;

    public AiCategoriser(GeminiClient gemini, JsonMapper jsonMapper) {
        this.gemini = gemini;
        this.jsonMapper = jsonMapper;
    }

    public record Item(String description, BigDecimal amount) {
    }

    record AiResult(List<AiItem> items) {
    }

    record AiItem(int index, String category) {
    }

    /** Returns a map of item position to category; positions Gemini couldn't place are left out. */
    public Map<Integer, Category> categorise(List<Item> items) {
        Map<Integer, Category> results = new HashMap<>();
        if (!gemini.isEnabled() || items.isEmpty()) {
            return results;
        }
        for (int start = 0; start < items.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, items.size());
            try {
                results.putAll(categoriseBatch(items.subList(start, end), start));
            } catch (RuntimeException e) {
                log.warn("AI categorisation failed, keeping rule results: {}", e.getMessage());
            }
        }
        return results;
    }

    private Map<Integer, Category> categoriseBatch(List<Item> batch, int offset) {
        StringBuilder prompt = new StringBuilder("""
                You categorise South African bank transactions.
                For each numbered transaction, choose the single best category from the allowed list.
                Negative amounts are money out; positive amounts are money in.
                If you are not confident, use OTHER.

                Transactions:
                """);
        for (int i = 0; i < batch.size(); i++) {
            Item item = batch.get(i);
            prompt.append(i).append(": ").append(item.description())
                  .append(" | ").append(item.amount().toPlainString()).append('\n');
        }

        String json = gemini.generateJson(prompt.toString(), responseSchema());
        AiResult result = jsonMapper.readValue(json, AiResult.class);

        Map<Integer, Category> categories = new HashMap<>();
        for (AiItem aiItem : result.items()) {
            Category category = Category.fromText(aiItem.category());
            boolean validIndex = aiItem.index() >= 0 && aiItem.index() < batch.size();
            if (validIndex && category != null && category != Category.OTHER) {
                categories.put(offset + aiItem.index(), category);
            }
        }
        return categories;
    }

    private static Map<String, Object> responseSchema() {
        List<String> categoryNames = Arrays.stream(Category.values()).map(Enum::name).toList();
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "items", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "index", Map.of("type", "INTEGER"),
                                                "category", Map.of("type", "STRING", "enum", categoryNames)),
                                        "required", List.of("index", "category")))),
                "required", List.of("items"));
    }
}