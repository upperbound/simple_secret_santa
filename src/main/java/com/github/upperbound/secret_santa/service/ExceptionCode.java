package com.github.upperbound.secret_santa.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {
    EMAIL_PARSING_EXCEPTION(10, HttpStatus.NOT_ACCEPTABLE, "Невозможно сформировать письмо для отправки на email: "),
    EMAIL_DELIVERY_EXCEPTION(11, HttpStatus.NOT_ACCEPTABLE, "Невозможно отправить письмо на указанный email: "),
    ;
    private final int code;
    private final HttpStatus httpStatus;
    private final String description;
}
