package com.example.common.dto.media.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMediaFilesRequestDTO{

    private List<String> fileNames;

}
