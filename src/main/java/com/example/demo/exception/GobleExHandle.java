package com.example.demo.exception;

import com.example.demo.model.entity.Result;
import com.example.demo.model.entity.StatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GobleExHandle {
    @ExceptionHandler(UsernameNotFoundException.class)
    public Result handle(UsernameNotFoundException e){
        return Result.error(StatusCode.LOGINERROR,e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handle(RuntimeException e){
        return Result.error(StatusCode.ERROR,e.getMessage());
    }


    @ExceptionHandler(IOException.class)
    public Result handle(IOException e){
        return Result.error(StatusCode.ERROR,e.getMessage());
    }

}
