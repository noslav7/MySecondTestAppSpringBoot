package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.stereotype.Service;
import ru.rachkov.MySecondTestAppSpringBoot.model.Request;

@Service
public class ModifySourceRequestService implements ModifyRequestService {

    @Override
    public void modify(Request request) {
        request.setSource("Service 1");
    }
}
