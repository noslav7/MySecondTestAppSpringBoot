package ru.rachkov.MySecondTestAppSpringBoot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    /** Уникальный идентификатор запроса; обязательное поле, не более 32 символов. */
    @NotBlank(message = "обязательное поле не должно быть пустым")
    @Size(max = 32, message = "длина не должна превышать 32 символа")
    private String uid;

    /** Уникальный идентификатор операции; не более 32 символов. */
    @Size(max = 32, message = "длина не должна превышать 32 символа")
    private String operationUid;

    /** Наименование системы, отправившей запрос. */
    private String systemName;

    /** Дата и время формирования запроса в системе-источнике. */
    private String systemTime;

    /** Источник запроса; при передаче следующему сервису устанавливается Service 1. */
    private String source;

    /** Должность сотрудника; передаётся вместе с salary, bonus и workDays для расчёта премии. */
    private Positions position;

    /** Оклад сотрудника; неотрицательное значение. */
    @PositiveOrZero(message = "оклад не может быть отрицательным")
    private Double salary;

    /** Множитель премии; неотрицательное значение. */
    @PositiveOrZero(message = "множитель премии не может быть отрицательным")
    private Double bonus;

    /** Количество отработанных дней за год; положительное значение. */
    @Positive(message = "количество рабочих дней должно быть больше нуля")
    private Integer workDays;

    /** Время приёма запроса Service 1 в миллисекундах Unix; устанавливается сервером. */
    private Long service1ReceivedAt;

    /** Идентификатор коммуникации; обязательное целое число от 1 до 100000. */
    @NotNull(message = "обязательное поле")
    @Min(value = 1, message = "значение должно быть не меньше 1")
    @Max(value = 100000, message = "значение должно быть не больше 100000")
    private Integer communicationId;

    /** Идентификатор шаблона коммуникации. */
    private Integer templateId;

    /** Код продукта, к которому относится коммуникация. */
    private Integer productCode;

    /** Код SMS-сообщения. */
    private Integer smsCode;

    @Override
    public String toString() {
        return "{" +
                "uid='" + uid + '\'' +
                ", operationUid='" + operationUid + '\'' +
                ", systemName='" + systemName + '\'' +
                ", systemTime='" + systemTime + '\'' +
                ", source='" + source + '\'' +
                ", position=" + position +
                ", salary=" + salary +
                ", bonus=" + bonus +
                ", workDays=" + workDays +
                ", service1ReceivedAt=" + service1ReceivedAt +
                ", communicationId=" + communicationId +
                ", templateId=" + templateId +
                ", productCode=" + productCode +
                ", smsCode=" + smsCode +
                '}';
    }
}
