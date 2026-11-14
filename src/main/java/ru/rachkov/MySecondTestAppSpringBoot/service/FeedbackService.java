package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ru.rachkov.MySecondTestAppSpringBoot.exception.UnsupportedCodeException;
import ru.rachkov.MySecondTestAppSpringBoot.exception.ValidationFailedException;
import ru.rachkov.MySecondTestAppSpringBoot.model.Codes;
import ru.rachkov.MySecondTestAppSpringBoot.model.ErrorCodes;
import ru.rachkov.MySecondTestAppSpringBoot.model.ErrorMessages;
import ru.rachkov.MySecondTestAppSpringBoot.model.Request;
import ru.rachkov.MySecondTestAppSpringBoot.model.Response;
import ru.rachkov.MySecondTestAppSpringBoot.util.DateTimeUtil;

import java.util.Date;

@Service
public class FeedbackService {

    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;
    private final ModifyRequestService modifyRequestService;
    private final AnnualBonusService annualBonusService;

    public FeedbackService(ValidationService validationService,
                           @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService,
                           ModifyRequestService modifyRequestService,
                           AnnualBonusService annualBonusService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifyRequestService = modifyRequestService;
        this.annualBonusService = annualBonusService;
    }

    public Response createResponse(Request request) {
        return Response.builder()
                .uid(request.getUid())
                .operationUid(request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();
    }

    public Response process(Request request, BindingResult bindingResult, Response response)
            throws ValidationFailedException, UnsupportedCodeException {
        validationService.isValid(bindingResult);
        if ("123".equals(request.getUid())) {
            throw new UnsupportedCodeException("Не поддерживаемая ошибка");
        }

        Double annualBonus = calculateAnnualBonus(request);
        response = modifyResponseService.modify(response);
        modifyRequestService.modify(request);
        response.setAnnualBonus(annualBonus);
        return response;
    }

    private Double calculateAnnualBonus(Request request) throws ValidationFailedException {
        if (request.getPosition() == null && request.getSalary() == null
                && request.getBonus() == null && request.getWorkDays() == null) {
            return null;
        }
        if (request.getPosition() == null || request.getSalary() == null
                || request.getBonus() == null || request.getWorkDays() == null) {
            throw new ValidationFailedException(
                    "Для расчёта премии нужно передать position, salary, bonus и workDays вместе");
        }
        return annualBonusService.calculate(request.getPosition(), request.getSalary(),
                request.getBonus(), request.getWorkDays());
    }
}
