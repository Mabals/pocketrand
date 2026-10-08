package io.github.mabals.pocketrand.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.github.mabals.pocketrand.dto.ImportResult;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.Transaction;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.TransactionRepository;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class StatementImportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategorisationService categorisationService;

    public StatementImportService(TransactionRepository transactionRepository,
                                UserRepository userRepository,
                                CategorisationService categorisationService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categorisationService = categorisationService;
    }

    private record ParsedRow(LocalDate date, String description, BigDecimal amount, Category category) {
    }

    @Transactional
    public ImportResult importCsv(MultipartFile file, Long userId) {
        List<String> lines = readLines(file);
        User owner = userRepository.getReferenceById(userId);

        List<ImportResult.ImportError> errors = new ArrayList<>();
        Set<String> seenInFile = new HashSet<>();
        List<ParsedRow> rowsToSave = new ArrayList<>();
        int duplicates = 0;

        // Pass 1: parse and check every row
        for (int i = 1; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                ParsedRow row = parseRow(line);
                String key = row.date() + "|" + row.description() + "|" + row.amount();
                boolean duplicateInFile = !seenInFile.add(key);
                boolean duplicateInDatabase = transactionRepository.existsByUserIdAndDateAndDescriptionAndAmount(
                        userId, row.date(), row.description(), row.amount());
                if (duplicateInFile || duplicateInDatabase) {
                    duplicates++;
                } else {
                    rowsToSave.add(row);
                }
            } catch (IllegalArgumentException e) {
                errors.add(new ImportResult.ImportError(lineNumber, e.getMessage()));
            }
        }

        // Pass 2: categorise all valid rows together (rules first, then one AI request)
        List<CategorisationService.Input> inputs = rowsToSave.stream()
                .map(row -> new CategorisationService.Input(row.category(), row.description(), row.amount()))
                .toList();
        List<KeywordCategoriser.CategoryDecision> decisions = categorisationService.decideAll(inputs);

        // Pass 3: save
        for (int i = 0; i < rowsToSave.size(); i++) {
            ParsedRow row = rowsToSave.get(i);
            KeywordCategoriser.CategoryDecision decision = decisions.get(i);
            transactionRepository.save(new Transaction(owner, row.date(), row.description(),
                    row.amount(), decision.category(), decision.source()));
        }

        return new ImportResult(rowsToSave.size(), errors.size(), duplicates, errors);
    }

    private List<String> readLines(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The file is empty");
        }
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only .csv files are supported");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file");
        }
    }

    private ParsedRow parseRow(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException("Expected 3 or 4 columns, got " + parts.length);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(parts[0].trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date '" + parts[0].trim() + "' (use YYYY-MM-DD)");
        }

        String description = parts[1].trim();
        if (description.isEmpty() || description.length() > 255) {
            throw new IllegalArgumentException("Description must be 1 to 255 characters");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(parts[2].trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount '" + parts[2].trim() + "'");
        }
        if (amount.signum() == 0) {
            throw new IllegalArgumentException("Amount cannot be zero");
        }

        Category category = parts.length == 4 ? Category.fromText(parts[3]) : null;
        return new ParsedRow(date, description, amount, category);
    }
}