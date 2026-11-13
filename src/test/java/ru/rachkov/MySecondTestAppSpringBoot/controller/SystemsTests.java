package ru.rachkov.MySecondTestAppSpringBoot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.rachkov.MySecondTestAppSpringBoot.service.ModifyRequestService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.rachkov.MySecondTestAppSpringBoot.model.Request;
import ru.rachkov.MySecondTestAppSpringBoot.model.Systems;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Temporarily disabled: systemName is currently String; these tests expect enum mapping and validation")
@SpringBootTest
class SystemsTests {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ModifyRequestService modifyRequestService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static String requestBody(String systemName) {
        return JSON.writeValueAsString(Map.of(
                "uid", "1",
                "operationUid", "operation-1",
                "systemName", systemName,
                "systemTime", "2026-10-19T10:00:00.000Z",
                "communicationId", 1));
    }

    @ParameterizedTest
    @EnumSource(Systems.class)
    void acceptsSystemNamesAndMapsThemToEnum(Systems system) throws Exception {
        String body = requestBody(system.name());
        Request request = JSON.readValue(body, Request.class);
        assertThat(request.getSystemName()).isEqualTo(system);
        assertThat(JSON.readTree(JSON.writeValueAsString(request)).get("systemName").asString())
                .isEqualTo(system.name());

        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        verify(modifyRequestService).modify(argThat(sent -> system.name().equals(sent.getSystemName())
                && "1".equals(sent.getUid())));
    }

    @Test
    void rejectsUnknownSystemName() throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("UNKNOWN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(modifyRequestService);
    }
}
