package com.mastery_task.advice;

import com.mastery_task.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.sql.Timestamp;
import java.util.Date;

@RestControllerAdvice
public class ExceptionAdvice {

    Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        printException(ex);

        return new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleAllOtherException(Exception ex, WebRequest request) {
        printException(ex);

        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    private void printException(Exception ex) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logger.error("EXCEPTION time->" + timestamp);
        logger.error(ex.getMessage(), ex);
        logger.error("\n");

        ex.printStackTrace();
    }
}