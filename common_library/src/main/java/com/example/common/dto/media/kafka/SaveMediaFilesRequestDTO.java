package com.example.common.dto.media.kafka;

import com.example.common.enumeration.media_service.BucketEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveMediaFilesRequestDTO {

    private List<FileDataDTO> fileDataDTOs;

    private BucketEnum bucket;

    private String requestKey;


}

