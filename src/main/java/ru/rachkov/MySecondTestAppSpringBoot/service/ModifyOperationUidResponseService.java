package ru.rachkov.MySecondTestAppSpringBoot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.rachkov.MySecondTestAppSpringBoot.model.Response;

import java.util.UUID;

@Slf4j
@Service
@Qualifier("ModifyOperationUidResponseService")
public class ModifyOperationUidResponseService
        implements ModifyResponseService {

    @Override
    public Response modify(Response response) {
        log.info("Response before changing operationUid: {}", response);
        UUID uuid = UUID.randomUUID();

        response.setOperationUid(uuid.toString());
        log.info("Response after changing operationUid: {}", response);

        return response;
    }
}
