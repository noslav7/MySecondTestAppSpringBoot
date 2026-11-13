package ru.rachkov.MySecondTestAppSpringBoot.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AnnualBonusControllerTests {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    private MockMvc mockMvc;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @AfterEach
    void verifyForwarding() {
        try {
            server.verify();
        } finally {
            server.reset();
        }
    }

    @Test
    void returnsAnnualBonusAndForwardsCalculationFields() throws Exception {
        server.expect(requestTo("http://localhost:8084/feedback"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath("$.position").value("DEV"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.salary").value(100000.0))
                .andExpect(MockRestRequestMatchers.jsonPath("$.bonus").value(1.5))
                .andExpect(MockRestRequestMatchers.jsonPath("$.workDays").value(250))
                .andRespond(withSuccess());

        send(bonusRequest()).andExpect(status().isOk())
                .andExpect(jsonPath("$.annualBonus").value(closeTo(481800.0, 0.000001)))
                .andExpect(jsonPath("$.uid").value("3"));
    }

    @Test
    void acceptsOriginalRequestWithoutCalculationFields() throws Exception {
        server.expect(requestTo("http://localhost:8084/feedback")).andRespond(withSuccess());

        send(baseRequest()).andExpect(status().isOk())
                .andExpect(jsonPath("$.annualBonus").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"position", "salary", "bonus", "workDays"})
    void rejectsIncompleteCalculationData(String missingField) throws Exception {
        Map<String, Object> request = bonusRequest();
        request.remove(missingField);

        send(request).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ValidationException"))
                .andExpect(jsonPath("$.annualBonus").isEmpty());
    }

    static Stream<Arguments> invalidNumbers() {
        return Stream.of(
                Arguments.of("salary", -1),
                Arguments.of("bonus", -0.1),
                Arguments.of("workDays", 0),
                Arguments.of("workDays", -1));
    }

    @ParameterizedTest
    @MethodSource("invalidNumbers")
    void rejectsInvalidNumbers(String field, Number value) throws Exception {
        Map<String, Object> request = bonusRequest();
        request.put(field, value);

        send(request).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ValidationException"));
    }

    @Test
    void rejectsUnknownPosition() throws Exception {
        Map<String, Object> request = bonusRequest();
        request.put("position", "UNKNOWN");

        send(request).andExpect(status().isBadRequest());
    }

    private ResultActions send(Map<String, Object> request) throws Exception {
        return mockMvc.perform(post("/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(request)));
    }

    private static Map<String, Object> baseRequest() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("uid", "3");
        request.put("operationUid", "");
        request.put("systemName", "ERP");
        request.put("systemTime", "");
        request.put("source", "");
        request.put("communicationId", 100);
        request.put("templateId", 8);
        request.put("productCode", 1500);
        request.put("smsCode", 10);
        return request;
    }

    private static Map<String, Object> bonusRequest() {
        Map<String, Object> request = baseRequest();
        request.put("position", "DEV");
        request.put("salary", 100000.0);
        request.put("bonus", 1.5);
        request.put("workDays", 250);
        return request;
    }
}
