package ru.rachkov.MySecondTestAppSpringBoot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MyControllerTests {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static Map<String, Object> validRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("uid", "1");
        body.put("operationUid", "operation-1");
        body.put("systemTime", "2026-10-06T10:00:00.000Z");
        body.put("communicationId", 1);
        return body;
    }

    private ResultActions send(Map<String, Object> body) throws Exception {
        Instant beforeRequest = Instant.ofEpochMilli(System.currentTimeMillis());
        return mockMvc.perform(post("/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)))
                .andExpect(result -> {
                    Instant afterRequest = Instant.ofEpochMilli(System.currentTimeMillis());
                    Map<?, ?> response = JSON.readValue(result.getResponse().getContentAsString(), Map.class);
                    Instant responseTime = Instant.parse((String) response.get("systemTime"));
                    assertThat(responseTime).isBetween(beforeRequest, afterRequest);
                });
    }

    private ResultActions expectValidationFailure(Map<String, Object> body) throws Exception {
        return send(body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("ValidationException"))
                .andExpect(jsonPath("$.errorMessage", containsString("Ошибка валидации")));
    }

    static Stream<Arguments> invalidValues() {
        Stream<Arguments> requiredStrings = Stream.of("uid", "operationUid", "systemTime")
                .flatMap(field -> Stream.of(null, "", "   ")
                        .map(value -> Arguments.of(field, value)));
        return Stream.concat(requiredStrings, Stream.of(
                Arguments.of("uid", "a".repeat(33)),
                Arguments.of("operationUid", "b".repeat(33)),
                Arguments.of("communicationId", null),
                Arguments.of("communicationId", -1),
                Arguments.of("communicationId", 0),
                Arguments.of("communicationId", 100001)));
    }

    @ParameterizedTest(name = "{0} = [{1}] must fail")
    @MethodSource("invalidValues")
    void rejectsInvalidField(String field, Object value) throws Exception {
        Map<String, Object> body = validRequest();
        body.put(field, value);
        expectValidationFailure(body)
                .andExpect(jsonPath("$.errorMessage", containsString(field + ":")));
    }

    static Stream<String> requiredFields() {
        return Stream.of("uid", "operationUid", "systemTime", "communicationId");
    }

    @ParameterizedTest
    @MethodSource("requiredFields")
    void rejectsMissingField(String field) throws Exception {
        Map<String, Object> body = validRequest();
        body.remove(field);
        expectValidationFailure(body)
                .andExpect(jsonPath("$.errorMessage", containsString(field + ":")));
    }

    static Stream<Arguments> validBoundaries() {
        return Stream.of(Arguments.of("1", "operation-1", 1),
                Arguments.of("1", "operation-1", 100000),
                Arguments.of("a".repeat(32), "b".repeat(32), 1));
    }

    @ParameterizedTest
    @MethodSource("validBoundaries")
    void acceptsValidBoundariesAndAbsentOptionalFields(String uid, String operationUid,
                                                      int communicationId) throws Exception {
        Map<String, Object> body = validRequest();
        body.put("uid", uid);
        body.put("operationUid", operationUid);
        body.put("communicationId", communicationId);
        send(body).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"))
                .andExpect(jsonPath("$.errorCode").value(""))
                .andExpect(jsonPath("$.errorMessage").value(""))
                .andExpect(jsonPath("$.uid").value(uid))
                .andExpect(jsonPath("$.uuid").doesNotExist())
                .andExpect(jsonPath("$.operationUid").value(operationUid));
    }

    @Test
    void acceptsOptionalFields() throws Exception {
        Map<String, Object> body = validRequest();
        body.put("systemName", "ERP");
        body.put("source", "Postman");
        body.put("templateId", 1);
        body.put("productCode", 2);
        body.put("smsCode", 3);
        send(body).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("success"));
    }

    @Test
    void reportsAllMissingFieldsTogether() throws Exception {
        ResultActions result = expectValidationFailure(Map.of());
        for (String field : requiredFields().toList()) {
            result.andExpect(jsonPath("$.errorMessage", containsString(field + ":")));
        }
    }

    @Test
    void reportsAllInvalidFieldsTogether() throws Exception {
        Map<String, Object> body = validRequest();
        body.put("uid", "a".repeat(33));
        body.put("operationUid", "b".repeat(33));
        body.put("systemTime", " ");
        body.put("communicationId", 100001);
        ResultActions result = expectValidationFailure(body);
        for (String field : requiredFields().toList()) {
            result.andExpect(jsonPath("$.errorMessage", containsString(field + ":")));
        }
    }

    @Test
    void returnsUnsupportedCodeExceptionForUid123() throws Exception {
        Map<String, Object> body = validRequest();
        body.put("uid", "123");
        send(body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uid").value("123"))
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("UnsupportedCodeException"))
                .andExpect(jsonPath("$.errorMessage").value("Не поддерживаемая ошибка"));
    }

    @Test
    void validatesFieldsBeforeCheckingUnsupportedCode() throws Exception {
        Map<String, Object> body = validRequest();
        body.put("uid", "123");
        body.remove("systemTime");
        expectValidationFailure(body)
                .andExpect(jsonPath("$.errorMessage", containsString("systemTime:")));
    }
}
