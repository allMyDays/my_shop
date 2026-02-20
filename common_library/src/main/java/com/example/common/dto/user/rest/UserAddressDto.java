package com.example.common.dto.user.rest;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserAddressDto {

    private String fullAddress;

    private double latitude;

    private double longitude;


}
