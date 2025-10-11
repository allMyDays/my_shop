package com.example.common.dto.media.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class FileDataDTO {

    private byte[] bytes;

    private String contentType;

}
