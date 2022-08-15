package com.media.quickmedia.restcontroller.error;

public class RestControllerRequestException extends RuntimeException{
    public RestControllerRequestException(Throwable throwable){
        super(throwable);
    }
}
