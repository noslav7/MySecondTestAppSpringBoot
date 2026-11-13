package ru.rachkov.MySecondTestAppSpringBoot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.rachkov.MySecondTestAppSpringBoot.exception.ValidationFailedException;
import ru.rachkov.MySecondTestAppSpringBoot.exception.UnsupportedCodeException;
import ru.rachkov.MySecondTestAppSpringBoot.model.Codes;
import ru.rachkov.MySecondTestAppSpringBoot.model.ErrorCodes;
import ru.rachkov.MySecondTestAppSpringBoot.model.ErrorMessages;
import ru.rachkov.MySecondTestAppSpringBoot.model.Request;
import ru.rachkov.MySecondTestAppSpringBoot.model.Response;
import ru.rachkov.MySecondTestAppSpringBoot.service.AnnualBonusService;
import ru.rachkov.MySecondTestAppSpringBoot.service.ValidationService;
import ru.rachkov.MySecondTestAppSpringBoot.service.ModifyRequestService;
import ru.rachkov.MySecondTestAppSpringBoot.service.ModifyResponseService;
import ru.rachkov.MySecondTestAppSpringBoot.util.DateTimeUtil;
import ru.rachkov.MySecondTestAppSpringBoot.filter.RequestReceivedTimeFilter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@RestController
public class MyController {

    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;
    private final ModifyRequestService modifyRequestService;
    private final AnnualBonusService annualBonusService;

    public MyController(ValidationService validationService,
                        @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService,
                        ModifyRequestService modifyRequestService,
                        AnnualBonusService annualBonusService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifyRequestService = modifyRequestService;
        this.annualBonusService = annualBonusService;
    }

    @PostMapping(value = "/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
                                              BindingResult bindingResult, HttpServletRequest httpRequest) {

        // Always overwrite the incoming value: Service 1 owns this timestamp.
        request.setService1ReceivedAt(RequestReceivedTimeFilter.receivedAt(httpRequest));
        log.info("request: {}", request);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Response response = Response.builder()
                .uid(request.getUid())
                .operationUid(request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();
        log.info("Created response: {}", response);

        try {
            validationService.isValid(bindingResult);
            if ("123".equals(request.getUid())) {
                log.error("Unsupported request uid={}: Не поддерживаемая ошибка", request.getUid());
                throw new UnsupportedCodeException("Не поддерживаемая ошибка");
            }
            Double annualBonus = calculateAnnualBonus(request);
            response = modifyResponseService.modify(response);
            modifyRequestService.modify(request);
            response.setAnnualBonus(annualBonus);
        } catch (ValidationFailedException e) {
            log.error("Request validation failed, uid={}: {}", request.getUid(), e.getMessage(), e);
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.VALIDATION_EXCEPTION);
            response.setErrorMessage(ErrorMessages.VALIDATION);
            return buildResponse(response, HttpStatus.BAD_REQUEST);
        } catch (UnsupportedCodeException e) {
            log.error("Request is unsupported, uid={}: {}", request.getUid(), e.getMessage(), e);
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNSUPPORTED_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNSUPORTED);
            return buildResponse(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected request processing error, uid={}", request.getUid(), e);
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNKNOWN_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNKNOWN);
            return buildResponse(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return buildResponse(response, HttpStatus.OK);
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

    private ResponseEntity<Response> buildResponse(Response response, HttpStatus status) {
        log.info("Returning response, status={}: {}", status.value(), response);
        return new ResponseEntity<>(response, status);
    }
}
