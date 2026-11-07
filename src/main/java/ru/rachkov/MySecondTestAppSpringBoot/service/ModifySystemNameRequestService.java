package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.rachkov.MySecondTestAppSpringBoot.model.Request;

@Service
@Primary
public class ModifySystemNameRequestService implements ModifyRequestService {

    private final ModifySourceRequestService modifySourceRequestService;
    private final RestTemplate restTemplate;

    public ModifySystemNameRequestService(ModifySourceRequestService modifySourceRequestService,
                                          RestTemplate restTemplate) {
        this.modifySourceRequestService = modifySourceRequestService;
        this.restTemplate = restTemplate;
    }

    @Override
    public void modify(Request request) {
        request.setSystemName("Service 1");
        modifySourceRequestService.modify(request);

        HttpEntity<Request> httpEntity = new HttpEntity<>(request);

        restTemplate.exchange(
                "http://localhost:8084/feedback",
                HttpMethod.POST,
                httpEntity,
                Void.class);
    }
}
