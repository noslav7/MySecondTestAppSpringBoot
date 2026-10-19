package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.rachkov.MySecondTestAppSpringBoot.model.Response;
import ru.rachkov.MySecondTestAppSpringBoot.util.DateTimeUtil;

import java.util.Date;
import java.util.UUID;

@Service
@Qualifier("ModifySystemTimeResponseService")
public class ModifySystemTimeResponseService
        implements ModifyResponseService {

    @Override
    public Response modify(Response response) {

        response.setSystemTime(DateTimeUtil.getCustomFormat()
                .format(new Date()));

        return response;
    }
}
