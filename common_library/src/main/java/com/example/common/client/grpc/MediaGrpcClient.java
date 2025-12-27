package com.example.common.client.grpc;

import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.enumeration.media_service.BucketEnum;
import com.example.common.grpc.media.Media;
import com.example.common.grpc.media.MediaServiceGrpc;
import com.example.common.mapper.MediaMapper;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.example.common.service.CommonMediaService.validateImages;

@Service
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class,
        EurekaClient.class
})
@RequiredArgsConstructor
public class MediaGrpcClient {

    private final ObjectProvider<MediaServiceGrpc.MediaServiceBlockingStub> blockingStubObjectProvider;

    private final MediaMapper mediaMapper;


    public List<String> uploadPhotos(List<MultipartFile> multipartFileList, BucketEnum bucketEnum){

        validateImages(multipartFileList);

        List<PhotoDataDTO> photoDataDTOList = multipartFileList.stream()
                .map(f->{
                    try {
                        return new PhotoDataDTO(f.getBytes(),f.getContentType());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                })
                .toList();



        Media.UploadPhotosRequest uploadPhotosRequest = Media.UploadPhotosRequest
                .newBuilder()
                .setBucketName(mediaMapper.toGrpcBucketName(bucketEnum))
                .addAllPhotos(mediaMapper.toGrpcPhotoDataList(photoDataDTOList))
                .build();

        Media.UploadPhotosResponse uploadResponse = blockingStubObjectProvider.getObject()
                .uploadPhotos(uploadPhotosRequest);

        return uploadResponse.getFileNamesList();
    }




}
