package com.webank.webase.front.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableException extends RuntimeException {

    private static final long serialVersionUID = 5362049897047204734L;

    public static UnprocessableException noMessageException() {
        return new UnprocessableException();
    }


    public UnprocessableException(String message) {
        super(message);
    }

    private UnprocessableException() {
        super();
    }

    public static UnprocessableException emailVerificationCodeInvalidException() {
        return new UnprocessableException("EMAIL_VERIFICATION_CODE_INVALID");
    }
}


