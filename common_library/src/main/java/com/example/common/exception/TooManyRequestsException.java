package com.example.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class TooManyRequestsException extends Exception {

    @Getter
    @Setter
    private int status;



}
