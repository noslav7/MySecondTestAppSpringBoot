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

    @NotBlank(message = "обязательное поле не должно быть пустым")
    @Size(max = 32, message = "длина не должна превышать 32 символа")
    private String uid;

    @Size(max = 32, message = "длина не должна превышать 32 символа")
    private String operationUid;
    private String systemName;

    private String systemTime;

    private String source;

    // Данные для расчёта премии: передаются вместе или полностью отсутствуют.
    private Positions position;

    @PositiveOrZero(message = "оклад не может быть отрицательным")
    private Double salary;

    @PositiveOrZero(message = "множитель премии не может быть отрицательным")
    private Double bonus;

    @Positive(message = "количество рабочих дней должно быть больше нуля")
    private Integer workDays;

    // Service 1 receipt time in milliseconds since the Unix epoch.
    private Long service1ReceivedAt;

    @NotNull(message = "обязательное поле")
    @Min(value = 1, message = "значение должно быть не меньше 1")
    @Max(value = 100000, message = "значение должно быть не больше 100000")
    private Integer communicationId;

    private Integer templateId;
    private Integer productCode;
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
