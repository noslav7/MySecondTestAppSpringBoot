package ru.rachkov.MySecondTestAppSpringBoot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.rachkov.MySecondTestAppSpringBoot.exception.ValidationFailedException;
import ru.rachkov.MySecondTestAppSpringBoot.exception.UnsupportedCodeException;
import ru.rachkov.MySecondTestAppSpringBoot.filter.RequestReceivedTimeFilter;
import ru.rachkov.MySecondTestAppSpringBoot.model.Codes;
import ru.rachkov.MySecondTestAppSpringBoot.model.ErrorCodes;
import ru.rachkov.MySecondTestAppSpringBoot.model.ErrorMessages;
import ru.rachkov.MySecondTestAppSpringBoot.model.Request;
import ru.rachkov.MySecondTestAppSpringBoot.model.Response;
import ru.rachkov.MySecondTestAppSpringBoot.service.FeedbackService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyController {

    private final FeedbackService feedbackService;

    @PostMapping(value = "/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
                                             BindingResult bindingResult, HttpServletRequest httpRequest) {

        // Время приёма запроса задаёт Service 1, независимо от входящего значения.
        request.setService1ReceivedAt(RequestReceivedTimeFilter.receivedAt(httpRequest));
        log.info("request: {}", request);
        Response response = feedbackService.createResponse(request);

        try {
            response = feedbackService.process(request, bindingResult, response);
        } catch (ValidationFailedException e) {
            log.error("Request validation failed, uid={}: {}", request.getUid(), e.getMessage(), e);
            return errorResponse(response, HttpStatus.BAD_REQUEST,
                    ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION);
        } catch (UnsupportedCodeException e) {
            log.error("Request is unsupported, uid={}: {}", request.getUid(), e.getMessage(), e);
            return errorResponse(response, HttpStatus.BAD_REQUEST,
                    ErrorCodes.UNSUPPORTED_EXCEPTION, ErrorMessages.UNSUPORTED);
        } catch (Exception e) {
            log.error("Unexpected request processing error, uid={}", request.getUid(), e);
            return errorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCodes.UNKNOWN_EXCEPTION, ErrorMessages.UNKNOWN);
        }

        return buildResponse(response, HttpStatus.OK);
    }

    private ResponseEntity<Response> errorResponse(Response response, HttpStatus status,
                                                  ErrorCodes errorCode, ErrorMessages errorMessage) {
        response.setCode(Codes.FAILED);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return buildResponse(response, status);
    }

    private ResponseEntity<Response> buildResponse(Response response, HttpStatus status) {
        log.info("Returning response, status={}: {}", status.value(), response);
        return new ResponseEntity<>(response, status);
    }
}
