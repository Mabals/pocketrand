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

import io.github.mabals.pocketrand.ai.PdfStatementExtractor;
import io.github.mabals.pocketrand.dto.ImportResult;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.Transaction;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.TransactionRepository;
import io.github.mabals.pocketrand.repository.UserRepository;
import io.github.mabals.pocketrand.service.KeywordCategoriser.CategoryDecision;

@Service
public class StatementImportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategorisationService categorisationService;
    private final PdfStatementExtractor pdfExtractor;

    public StatementImportService(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  CategorisationService categorisationService,
                                  PdfStatementExtractor pdfExtractor) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categorisationService = categorisationService;
        this.pdfExtractor = pdfExtractor;
    }

    private record ParsedRow(LocalDate date, String description, BigDecimal amount, Category category) {
    }

    @Transactional
    public ImportResult importStatement(MultipartFile file, Long userId) {
        String fileName = checkFile(file);
        List<ImportResult.ImportError> errors = new ArrayList<>();

        List<ParsedRow> rows = fileName.endsWith(".pdf")
                ? parsePdf(file, errors)
                : parseCsv(file, errors);

        return saveNewRows(rows, userId, errors);
    }

    // ---------- Reading each format ----------

    private List<ParsedRow> parseCsv(MultipartFile file, List<ImportResult.ImportError> errors) {
        List<String> lines = readLines(file);
        List<ParsedRow> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {          // start at 1: skip the header row
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                String[] parts = line.split(",", -1);
                if (parts.length < 3 || parts.length > 4) {
                    throw new IllegalArgumentException("Expected 3 or 4 columns, got " + parts.length);
                }
                rows.add(toParsedRow(parts[0], parts[1], parts[2], parts.length == 4 ? parts[3] : null));
            } catch (IllegalArgumentException e) {
                errors.add(new ImportResult.ImportError(i + 1, e.getMessage()));
            }
        }
        return rows;
    }

    private List<ParsedRow> parsePdf(MultipartFile file, List<ImportResult.ImportError> errors) {
        List<PdfStatementExtractor.ExtractedRow> extracted = pdfExtractor.extract(readBytes(file));
        List<ParsedRow> rows = new ArrayList<>();
        for (int i = 0; i < extracted.size(); i++) {
            PdfStatementExtractor.ExtractedRow row = extracted.get(i);
            try {
                rows.add(toParsedRow(row.date(), row.description(), row.amount(), null));
            } catch (IllegalArgumentException e) {
                errors.add(new ImportResult.ImportError(i + 1, e.getMessage()));   // for PDFs: the row number
            }
        }
        return rows;
    }

    // ---------- Shared: duplicates, categorising, saving ----------

    private ImportResult saveNewRows(List<ParsedRow> rows, Long userId, List<ImportResult.ImportError> errors) {
        User owner = userRepository.getReferenceById(userId);
        Set<String> seenInFile = new HashSet<>();
        List<ParsedRow> rowsToSave = new ArrayList<>();
        int duplicates = 0;

        for (ParsedRow row : rows) {
            String key = row.date() + "|" + row.description() + "|" + row.amount();
            boolean duplicateInFile = !seenInFile.add(key);
            boolean duplicateInDatabase = transactionRepository.existsByUserIdAndDateAndDescriptionAndAmount(
                    userId, row.date(), row.description(), row.amount());
            if (duplicateInFile || duplicateInDatabase) {
                duplicates++;
            } else {
                rowsToSave.add(row);
            }
        }

        List<CategorisationService.Input> inputs = rowsToSave.stream()
                .map(row -> new CategorisationService.Input(row.category(), row.description(), row.amount()))
                .toList();
        List<CategoryDecision> decisions = categorisationService.decideAll(inputs);

        for (int i = 0; i < rowsToSave.size(); i++) {
            ParsedRow row = rowsToSave.get(i);
            CategoryDecision decision = decisions.get(i);
            transactionRepository.save(new Transaction(owner, row.date(), row.description(),
                    row.amount(), decision.category(), decision.source()));
        }
        return new ImportResult(rowsToSave.size(), errors.size(), duplicates, errors);
    }

    // ---------- Validation and helpers ----------

    private ParsedRow toParsedRow(String dateText, String descriptionText, String amountText, String categoryText) {
        String dateValue = dateText == null ? "" : dateText.trim();
        String description = descriptionText == null ? "" : descriptionText.trim();
        String amountValue = amountText == null ? "" : amountText.trim().replace(" ", "");

        LocalDate date;
        try {
            date = LocalDate.parse(dateValue);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date '" + dateValue + "' (use YYYY-MM-DD)");
        }

        if (description.isEmpty() || description.length() > 255) {
            throw new IllegalArgumentException("Description must be 1 to 255 characters");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountValue).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount '" + amountValue + "'");
        }
        if (amount.signum() == 0) {
            throw new IllegalArgumentException("Amount cannot be zero");
        }

        return new ParsedRow(date, description, amount, Category.fromText(categoryText));
    }

    private String checkFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The file is empty");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".csv") && !name.endsWith(".pdf")) {
            throw new IllegalArgumentException("Only .csv and .pdf statements are supported");
        }
        return name;
    }

    private List<String> readLines(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file");
        }
    }
}