package com.happymoney.cookiecutterservice.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {

    public static Error fromErrorCode(ErrorCodes err) {
        return Error.builder()
            .title(err.getTitle())
            .status(err.getStatus())
            .detail(err.getDetail())
            .build();
    }

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "detail")
    private String detail;

    public Error(String message) {
        this.status = ErrorCodes.valueOf(message).getStatus();
        this.title = ErrorCodes.valueOf(message).getTitle();
        this.detail = ErrorCodes.valueOf(message).getDetail();
    }
}
