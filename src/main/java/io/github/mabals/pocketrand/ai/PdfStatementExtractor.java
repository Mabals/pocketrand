package io.github.mabals.pocketrand.ai;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.mabals.pocketrand.exception.AiUnavailableException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PdfStatementExtractor {

    private static final Logger log = LoggerFactory.getLogger(PdfStatementExtractor.class);
    private static final int LINES_PER_REQUEST = 100;

    // A transaction line starts with a date: 2026-09-01, 01/09/2026, 01.09, 1 Sep or 01 September 2026
    private static final Pattern STARTS_WITH_DATE = Pattern.compile(
            "^\\s*(\\d{4}-\\d{2}-\\d{2}|\\d{1,2}[/.\\-]\\d{1,2}([/.\\-]\\d{2,4})?|\\d{1,2}\\s+[A-Za-z]{3,9}(\\s+\\d{4})?)\\b");
    // 8 or more digits in a row: likely an account, card or reference number
    private static final Pattern LONG_NUMBER = Pattern.compile("\\b\\d{8,}\\b");
    private static final Pattern YEAR = Pattern.compile("\\b(20\\d{2})\\b");

    private final GeminiClient gemini;
    private final JsonMapper jsonMapper;

    public PdfStatementExtractor(GeminiClient gemini, JsonMapper jsonMapper) {
        this.gemini = gemini;
        this.jsonMapper = jsonMapper;
    }

    public record ExtractedRow(String date, String description, String amount) {
    }

    record ExtractionResult(List<ExtractedRow> rows) {
    }

    public List<ExtractedRow> extract(byte[] pdfBytes) {
        if (!gemini.isEnabled()) {
            throw new IllegalArgumentException("PDF import needs the AI service, which isn't configured");
        }

        String text = readText(pdfBytes);
        String year = findYear(text);

        List<String> lines = text.lines()
                .filter(line -> STARTS_WITH_DATE.matcher(line).find())
                .map(line -> LONG_NUMBER.matcher(line).replaceAll("[number removed]"))
                .toList();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("No transaction lines found in this PDF");
        }
        log.info("Sending {} statement lines to the AI for extraction", lines.size());

        List<ExtractedRow> rows = new ArrayList<>();
        for (int start = 0; start < lines.size(); start += LINES_PER_REQUEST) {
            List<String> chunk = lines.subList(start, Math.min(start + LINES_PER_REQUEST, lines.size()));
            rows.addAll(extractChunk(chunk, year));
        }
        return rows;
    }

    private String readText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            String text = new PDFTextStripper().getText(document);
            if (text.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text in this PDF; scanned statements aren't supported yet");
            }
            return text;
        } catch (InvalidPasswordException e) {
            throw new IllegalArgumentException(
                    "This PDF is password-protected; remove the password and upload it again");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read this PDF");
        }
    }

    private static String findYear(String text) {
        Matcher matcher = YEAR.matcher(text);
        return matcher.find() ? matcher.group(1) : String.valueOf(Year.now().getValue());
    }

    private List<ExtractedRow> extractChunk(List<String> lines, String year) {
        String prompt = """
                Extract the bank transactions from these South African bank statement lines.
                Rules:
                - One output row per transaction. Skip opening balances, closing balances, totals and balance-only lines.
                - date: YYYY-MM-DD. If a line has no year, use %s.
                - description: only the transaction description, without amounts or balances.
                - amount: a plain number with 2 decimals and no thousands separators,
                  negative for money out (debits and fees), positive for money in (credits).

                Lines:
                %s
                """.formatted(year, String.join("\n", lines));
        try {
            String json = gemini.generateJson(prompt, responseSchema());
            return jsonMapper.readValue(json, ExtractionResult.class).rows();
        } catch (RuntimeException e) {
            log.warn("PDF extraction failed: {}", e.getMessage());
            throw new AiUnavailableException("Couldn't read the statement right now. Please try again later.");
        }
    }

    private static Map<String, Object> responseSchema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "rows", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "date", Map.of("type", "STRING"),
                                                "description", Map.of("type", "STRING"),
                                                "amount", Map.of("type", "STRING")),
                                        "required", List.of("date", "description", "amount")))),
                "required", List.of("rows"));
    }
}