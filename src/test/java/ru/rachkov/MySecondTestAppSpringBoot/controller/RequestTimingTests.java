package ru.rachkov.MySecondTestAppSpringBoot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ru.rachkov.MySecondTestAppSpringBoot.filter.RequestReceivedTimeFilter;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RequestTimingTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RequestReceivedTimeFilter receivedTimeFilter;

    @Test
    void forwardsModifiedSourceAndServerReceiptTime() throws Exception {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        long before = System.currentTimeMillis();
        server.expect(requestTo("http://localhost:8084/feedback"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.source").value("Service 1"))
                .andExpect(jsonPath("$.systemName").value("Service 1"))
                .andExpect(jsonPath("$.systemTime").value("2026-11-05T10:00:00.000Z"))
                .andExpect(outgoing -> {
                    var body = JsonMapper.builder().build().readTree(
                            ((org.springframework.mock.http.client.MockClientHttpRequest) outgoing).getBodyAsString());
                    assertThat(body.get("service1ReceivedAt").asLong())
                            .isBetween(before, System.currentTimeMillis());
                })
                .andRespond(withSuccess());

        try {
            MockMvcBuilders.webAppContextSetup(context).addFilters(receivedTimeFilter).build()
                    .perform(post("/feedback").contentType(MediaType.APPLICATION_JSON).content("""
                            {"uid":"timing-test", "communicationId":1, "source":"Postman",
                             "systemTime":"2026-11-05T10:00:00.000Z", "service1ReceivedAt":1}
                            """))
                    .andExpect(status().isOk());
            server.verify();
        } finally {
            server.reset();
        }
    }
}
