package ru.rachkov.MySecondTestAppSpringBoot.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {

    /** Уникальный идентификатор запроса, скопированный из Request. */
    private String uid;

    /** Уникальный идентификатор операции, скопированный из Request. */
    private String operationUid;

    /** Дата и время формирования ответа сервисом. */
    private String systemTime;

    /** Результат обработки запроса: SUCCESS — успех, FAILED — ошибка. */
    private Codes code;

    /** Код ошибки; EMPTY при успешной обработке. */
    private ErrorCodes errorCode;

    /** Описание ошибки; EMPTY при успешной обработке. */
    private ErrorMessages errorMessage;

    /** Рассчитанная годовая премия; null, если данные для расчёта отсутствуют или произошла ошибка. */
    private Double annualBonus;
}
