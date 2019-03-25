package com.webank.webase.front.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileException extends RuntimeException {

    public FileException(String message, Throwable cause){
        super(message, cause);
    }
}
