package ru.rachkov.MySecondTestAppSpringBoot.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
import ru.rachkov.MySecondTestAppSpringBoot.service.ValidationService;
import ru.rachkov.MySecondTestAppSpringBoot.util.DateTimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@RestController
public class MyController {

    private final ValidationService validationService;

    public MyController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping(value = "/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
                                              BindingResult bindingResult) {

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

    private ResponseEntity<Response> buildResponse(Response response, HttpStatus status) {
        log.info("Returning response, status={}: {}", status.value(), response);
        return new ResponseEntity<>(response, status);
    }
}
