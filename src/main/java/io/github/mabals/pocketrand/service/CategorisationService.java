package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.mabals.pocketrand.ai.AiCategoriser;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.CategorySource;
import io.github.mabals.pocketrand.service.KeywordCategoriser.CategoryDecision;

@Service
public class CategorisationService {

    private final KeywordCategoriser keywordCategoriser;
    private final AiCategoriser aiCategoriser;

    public CategorisationService(KeywordCategoriser keywordCategoriser, AiCategoriser aiCategoriser) {
        this.keywordCategoriser = keywordCategoriser;
        this.aiCategoriser = aiCategoriser;
    }

    public record Input(Category requested, String description, BigDecimal amount) {
    }

    public CategoryDecision decide(Category requested, String description, BigDecimal amount) {
        return decideAll(List.of(new Input(requested, description, amount))).getFirst();
    }

    public List<CategoryDecision> decideAll(List<Input> inputs) {
        // Step 1: user choice, then keyword rules (instant and free)
        List<CategoryDecision> decisions = new ArrayList<>(inputs.stream()
                .map(input -> keywordCategoriser.decide(input.requested(), input.description(), input.amount()))
                .toList());

        // Step 2: collect what the rules couldn't place
        List<Integer> unresolved = new ArrayList<>();
        for (int i = 0; i < decisions.size(); i++) {
            CategoryDecision decision = decisions.get(i);
            if (decision.source() == CategorySource.RULE && decision.category() == Category.OTHER) {
                unresolved.add(i);
            }
        }
        if (unresolved.isEmpty()) {
            return decisions;
        }

        // Step 3: ask the AI once for all of them
        List<AiCategoriser.Item> items = unresolved.stream()
                .map(i -> new AiCategoriser.Item(inputs.get(i).description(), inputs.get(i).amount()))
                .toList();
        Map<Integer, Category> aiResults = aiCategoriser.categorise(items);
        aiResults.forEach((position, category) ->
                decisions.set(unresolved.get(position), new CategoryDecision(category, CategorySource.AI)));

        return decisions;
    }
}