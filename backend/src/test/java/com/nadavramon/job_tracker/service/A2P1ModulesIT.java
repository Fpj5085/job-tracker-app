package com.nadavramon.job_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class A2P1ModulesIT {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.nadavramon.job_tracker.service.JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User alice;
    private User bob;

    @BeforeEach
    void setUpUsers() {
        alice = userRepository.findByUsername("a2alice").orElseGet(() -> saveUser("a2alice"));
        bob = userRepository.findByUsername("a2bob").orElseGet(() -> saveUser("a2bob"));
    }

    private User saveUser(String username) {
        User user = new User();
        user.setEmail(username + "@example.com");
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("Password123!"));
        return userRepository.save(user);
    }

    private Cookie authCookie(User user) {
        return new Cookie("jwt", jwtService.generateToken(user.getUsername()));
    }

    private String json(Map<String, Object> map) throws Exception {
        return JSON.writeValueAsString(map);
    }

    private UUID createApplication(User owner, String company) throws Exception {
        String body = json(Map.of("companyName", company, "jobRole", "Backend Engineer", "location", "Shanghai",
                "status", "APPLIED", "jobType", "FULL_TIME", "appliedDate", "2026-11-14"));
        String response = mockMvc.perform(post("/applications").cookie(authCookie(owner))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JSON.readTree(response).get("id").asText());
    }

    @Test
    void interviewCrudOwnershipAndUpcomingReminder() throws Exception {
        UUID appId = createApplication(alice, "InterviewCo");
        String body = json(Map.of("stage", "TECHNICAL", "scheduledAt", LocalDateTime.now().plusHours(10).toString(),
                "timezone", "Asia/Shanghai", "mode", "ONLINE"));
        String created = mockMvc.perform(post("/applications/{id}/interviews", appId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stage").value("TECHNICAL"))
                .andReturn().getResponse().getContentAsString();
        String interviewId = JSON.readTree(created).get("id").asText();

        mockMvc.perform(get("/applications/{id}/interviews", appId).cookie(authCookie(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/interviews/upcoming?hours=24").cookie(authCookie(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/applications/{id}/interviews", appId).cookie(authCookie(bob)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/applications/{id}/interviews", appId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/applications/{id}/interviews/{iid}", appId, interviewId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("result", "PASS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("PASS"));
        mockMvc.perform(get("/interviews/upcoming?hours=24").cookie(authCookie(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(delete("/applications/{id}/interviews/{iid}", appId, interviewId).cookie(authCookie(alice)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/applications/{id}/interviews", appId).cookie(authCookie(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void communicationCrudValidationAndOwnership() throws Exception {
        UUID appId = createApplication(alice, "CommCo");
        String body = json(Map.of("channel", "EMAIL", "direction", "OUT",
                "occurredAt", LocalDateTime.now().minusHours(1).toString(),
                "summary", "Sent follow-up",
                "nextAction", "Await reply",
                "nextActionAt", LocalDateTime.now().plusDays(1).toString()));
        mockMvc.perform(post("/applications/{id}/communications", appId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary").value("Sent follow-up"))
                .andExpect(jsonPath("$.applicationId").value(appId.toString()));

        String invalid = json(Map.of("channel", "EMAIL", "direction", "OUT",
                "occurredAt", LocalDateTime.now().toString(), "summary", ""));
        mockMvc.perform(post("/applications/{id}/communications", appId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/applications/{id}/communications", appId).cookie(authCookie(bob)))
                .andExpect(status().isForbidden());
    }

    @Test
    void offerCrudAndComparison() throws Exception {
        UUID appId = createApplication(alice, "OfferCo");
        String cnyOffer = json(Map.of("baseSalary", 30000, "bonus", 60000, "equity", 50000,
                "currency", "CNY", "location", "Shanghai", "ptoDays", 10, "remotePolicy", "hybrid"));
        String usdOffer = json(Map.of("baseSalary", 10000, "currency", "USD", "location", "Remote"));
        mockMvc.perform(post("/applications/{id}/offers", appId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(cnyOffer))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("OfferCo"));
        mockMvc.perform(post("/applications/{id}/offers", appId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(usdOffer))
                .andExpect(status().isCreated());

        String comparison = mockMvc.perform(get("/offers/compare").cookie(authCookie(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("CNY"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        JsonNode root = JSON.readTree(comparison);
        assertEquals(0, new BigDecimal("870000.00").compareTo(new BigDecimal(root.get("items").get(0).get("normalizedAnnualPackage").asText())));
        assertEquals(0, new BigDecimal("470000.00").compareTo(new BigDecimal(root.get("items").get(1).get("normalizedAnnualPackage").asText())));

        String invalid = json(Map.of("baseSalary", -1, "currency", "CNY", "location", "X"));
        mockMvc.perform(post("/applications/{id}/offers", appId).cookie(authCookie(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/offers/compare").cookie(authCookie(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void applicationOwnershipMapsToForbidden() throws Exception {
        UUID appId = createApplication(alice, "OwnedCo");
        mockMvc.perform(delete("/applications/{id}", appId).cookie(authCookie(bob)))
                .andExpect(status().isForbidden());
    }
}
