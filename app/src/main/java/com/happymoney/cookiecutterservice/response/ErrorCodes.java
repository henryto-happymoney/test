package com.happymoney.cookiecutterservice.response;

import lombok.Getter;


public enum ErrorCodes {
;

    @Getter
    private final String status;
    @Getter
    private final String title;
    @Getter
    private final String detail;

    ErrorCodes(String status, String title, String detail) {
        this.status = status;
        this.title = title;
        this.detail = detail;
    }
}
