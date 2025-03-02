package com.github.upperbound.secret_santa.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * <p> Common business layer exception codes </p>
 * @author Vladislav Tsukanov
 */
@RequiredArgsConstructor
@Getter
public enum ExceptionCode {
    EMAIL_PARSING_EXCEPTION(10, HttpStatus.NOT_ACCEPTABLE, "Unable to parse email for this address: "),
    EMAIL_DELIVERY_EXCEPTION(11, HttpStatus.NOT_ACCEPTABLE, "Unable to send email to this address: "),
    PARTICIPANT_NOT_EXIST(12, HttpStatus.NOT_FOUND, "Participant with this email address does not exist: "),
    GROUP_NOT_EXIST(13, HttpStatus.NOT_FOUND, "Group with this uuid does not exist: "),
    ACTION_TOKEN_EXPIRED(14, HttpStatus.FORBIDDEN, "Action token expired"),
    ACTION_TOKEN_WRONG(15, HttpStatus.FORBIDDEN, "Wrong action token"),
    ;
    private final int code;
    private final HttpStatus httpStatus;
    private final String description;
}
