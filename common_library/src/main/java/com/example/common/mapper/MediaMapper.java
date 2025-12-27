package com.example.common.mapper;

import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.grpc.media.Media;
import com.google.protobuf.ByteString;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

import java.util.List;


@Mapper(componentModel = "spring")
public abstract class MediaMapper {


    public abstract Media.PhotoData toPhotoData(PhotoDataDTO photoDataDTO);

    public abstract List<Media.PhotoData> toGrpcPhotoDataList(List<PhotoDataDTO> photoDataDTOs);

    public abstract List<PhotoDataDTO> toPhotoDataDTOs(List<Media.PhotoData> photoDataList);

    public abstract Media.BucketName toGrpcBucketName(com.example.common.enumeration.media_service.BucketEnum bucketName);

    @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
    public abstract com.example.common.enumeration.media_service.BucketEnum toBucketName(Media.BucketName bucketName);

    public byte[] map(ByteString byteString) {
        return byteString==null?null: byteString.toByteArray();
    }

    public ByteString map(byte[] bytes) {
        return bytes==null?ByteString.EMPTY:ByteString.copyFrom(bytes);
    }











}
