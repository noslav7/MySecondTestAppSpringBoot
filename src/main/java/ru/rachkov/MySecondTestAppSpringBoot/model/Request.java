package ru.rachkov.MySecondTestAppSpringBoot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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

    @NotBlank(message = "обязательное поле не должно быть пустым")
    @Size(max = 32, message = "длина не должна превышать 32 символа")
    private String operationUid;
    private String systemName;

    @NotBlank(message = "обязательное поле не должно быть пустым")
    private String systemTime;
    private String source;

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
                ", communicationId=" + communicationId +
                ", templateId=" + templateId +
                ", productCode=" + productCode +
                ", smsCode=" + smsCode +
                '}';
    }
}
