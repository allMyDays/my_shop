package com.example.catalogue_service.controller;


import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BadRequestControllerAdvice {

   @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception) {

       ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"BAD_REQUEST:(");
       problemDetail.setProperty("errors",
               exception.getAllErrors().stream()
                       .map(DefaultMessageSourceResolvable::getDefaultMessage)
                       .toList()
       );
       return ResponseEntity.badRequest()
               .body(problemDetail);












    }






}
