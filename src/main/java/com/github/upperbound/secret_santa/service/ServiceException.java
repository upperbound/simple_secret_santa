package com.github.upperbound.secret_santa.service;

import lombok.Getter;

/**
 * <p> Exception that may occur on a business layer </p>
 * @author Vladislav Tsukanov
 */
@Getter
public class ServiceException extends Exception {
    private final ExceptionCode exceptionCode;
    private final String additionalInfo;

    public ServiceException(ExceptionCode exceptionCode) {
        this(exceptionCode, "");
    }

    public ServiceException(ExceptionCode exceptionCode, Throwable cause) {
        this(exceptionCode, "", cause);
    }

    public ServiceException(ExceptionCode exceptionCode, String additionalInfo) {
        super(exceptionCode.getDescription() + additionalInfo);
        this.exceptionCode = exceptionCode;
        this.additionalInfo = additionalInfo;
    }

    public ServiceException(ExceptionCode exceptionCode, String additionalInfo, Throwable cause) {
        super(exceptionCode.getDescription() + additionalInfo, cause);
        this.exceptionCode = exceptionCode;
        this.additionalInfo = additionalInfo;
    }

    public String getErrorDescription() {
        return exceptionCode.getDescription() + additionalInfo;
    }
}
