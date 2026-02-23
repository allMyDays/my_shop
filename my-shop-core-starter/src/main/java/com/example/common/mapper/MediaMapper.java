package com.example.common.mapper;

import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.grpc.media.Media;
import com.google.protobuf.ByteString;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Mapper(componentModel = "spring")
public abstract class MediaMapper {


    public abstract Media.PhotoData toPhotoData(PhotoDataDTO photoDataDTO);

    public abstract List<Media.PhotoData> toGrpcPhotoDataList(List<PhotoDataDTO> photoDataDTOs);

    public abstract List<PhotoDataDTO> toPhotoDataDTOs(List<Media.PhotoData> photoDataList);

    public abstract Media.BucketName toGrpcBucketName(com.example.common.enumeration.media.BucketEnum bucketName);

    @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
    public abstract com.example.common.enumeration.media.BucketEnum toBucketName(Media.BucketName bucketName);

    public byte[] map(ByteString byteString) {
        return byteString==null?null: byteString.toByteArray();
    }

    public ByteString map(byte[] bytes) {
        return bytes==null?ByteString.EMPTY:ByteString.copyFrom(bytes);
    }


    public List<PhotoDataDTO> toPhotoDataDtoList(List<MultipartFile> multipartFiles){
        return multipartFiles.stream()
                .map(f->{
                    try {
                        return new PhotoDataDTO(f.getBytes(),f.getContentType());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                })
                .toList();

    }











}
