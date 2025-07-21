package com.example.managerapp.exception;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
public class TooManyRequestsException extends Exception {

    @Getter
    @Setter
    private int status;



}
