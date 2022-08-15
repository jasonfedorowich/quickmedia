package com.media.quickmedia.restcontroller.support;

import com.media.quickmedia.restcontroller.error.RestControllerRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(RestControllerRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void handleBadRequest(RestControllerRequestException exception){
        log.info("Request threw an exception with trace: {}", exception.toString());
    }

}
