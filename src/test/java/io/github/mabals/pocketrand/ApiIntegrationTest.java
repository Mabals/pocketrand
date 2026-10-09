package io.github.mabals.pocketrand;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String PASSWORD = "Password123";

    private static final String SAMPLE_CSV = """
            Date,Description,Amount,Category
            2026-09-01,SALARY,25000.00,
            2026-09-02,RENT PAYMENT,-7500.00,
            2026-09-03,GROCERIES,-1250.50,
            2026-09-04,BAD ROW,abc,
            2026-09-02,RENT PAYMENT,-7500.00,
            """;

    @Autowired
    private MockMvc mockMvc;

    // ---------- Security ----------

    @Test
    void protectedEndpointsNeedAToken() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerThenLoginThenSeeOwnProfile() throws Exception {
        String email = uniqueEmail();
        register("Test User", email);
        String token = login(email);

        mockMvc.perform(get("/api/auth/me").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registrationRequiresPrivacyConsent() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("No Consent", uniqueEmail(), false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.acceptedPrivacy").exists());
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameError() throws Exception {
        String email = uniqueEmail();
        register("Real User", email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "WrongPassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(uniqueEmail(), PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void usersCannotSeeOrDeleteEachOthersTransactions() throws Exception {
        String thato = registerAndLogin("Thato");
        String lerato = registerAndLogin("Lerato");

        MvcResult created = mockMvc.perform(post("/api/transactions")
                        .header(AUTHORIZATION, bearer(thato))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-09-02\",\"description\":\"RENT PAYMENT\",\"amount\":-7500.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("HOUSING"))
                .andExpect(jsonPath("$.categorySource").value("RULE"))
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/transactions/" + id).header(AUTHORIZATION, bearer(lerato)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/transactions/" + id).header(AUTHORIZATION, bearer(lerato)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/transactions/" + id).header(AUTHORIZATION, bearer(thato)))
                .andExpect(status().isOk());
    }

    // ---------- Transactions and import ----------

    @Test
    void zeroAmountIsRejected() throws Exception {
        String token = registerAndLogin("Zero");

        mockMvc.perform(post("/api/transactions")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-09-05\",\"description\":\"Mistake\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount cannot be zero"));
    }

    @Test
    void csvImportSkipsDuplicatesAndReportsBadRows() throws Exception {
        String token = registerAndLogin("Importer");

        mockMvc.perform(multipart("/api/transactions/import").file(sampleCsv()).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.duplicates").value(1))
                .andExpect(jsonPath("$.skipped").value(1))
                .andExpect(jsonPath("$.errors[0].line").value(5));

        // The same file again: nothing new is saved
        mockMvc.perform(multipart("/api/transactions/import").file(sampleCsv()).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(0))
                .andExpect(jsonPath("$.duplicates").value(4));
    }

    // ---------- Insights and tax ----------

    @Test
    void monthlySummaryAddsUpCorrectly() throws Exception {
        String token = registerAndLogin("Summary");
        mockMvc.perform(multipart("/api/transactions/import").file(sampleCsv()).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/summary?month=2026-09").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(25000.0))
                .andExpect(jsonPath("$.expenses").value(8750.5))
                .andExpect(jsonPath("$.net").value(16249.5))
                .andExpect(jsonPath("$.spendingByCategory[0].category").value("HOUSING"))
                .andExpect(jsonPath("$.spendingByCategory[0].percentage").value(85.7));
    }

    @Test
    void taxEstimateMatchesTheSarsExample() throws Exception {
        String token = registerAndLogin("Tax");

        mockMvc.perform(post("/api/tax/estimate")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grossSalary\":30000,\"period\":\"MONTHLY\",\"age\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annualTax").value(56172.0))
                .andExpect(jsonPath("$.monthlyTakeHome").value(25141.88));
    }

    // ---------- Account ----------

    @Test
    void deletingAnAccountRemovesItsAccess() throws Exception {
        String email = uniqueEmail();
        register("Leaving User", email);
        String token = login(email);
        mockMvc.perform(multipart("/api/transactions/import").file(sampleCsv()).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/account")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Helpers ----------

    private String registerAndLogin(String name) throws Exception {
        String email = uniqueEmail();
        register(name, email);
        return login(email);
    }

    private void register(String name, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(name, email, true)))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String registerJson(String name, String email, boolean acceptedPrivacy) {
        return "{\"fullName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"acceptedPrivacy\":%s}"
                .formatted(name, email, PASSWORD, acceptedPrivacy);
    }

    private static String loginJson(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    private static MockMultipartFile sampleCsv() {
        return new MockMultipartFile("file", "statement.csv", "text/csv",
                SAMPLE_CSV.getBytes(StandardCharsets.UTF_8));
    }
}