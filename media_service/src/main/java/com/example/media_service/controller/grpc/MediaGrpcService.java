package com.example.media_service.controller.grpc;


import com.example.common.dto.product.ProductIdAndPriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.grpc.media.Media;
import com.example.common.grpc.media.MediaServiceGrpc;
import com.example.common.grpc.product.*;
import com.example.common.mapper.MediaMapper;
import com.example.media_service.service.MinioService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
//import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
//@Transactional
public class MediaGrpcService extends MediaServiceGrpc.MediaServiceImplBase {

    private final MinioService minioService;

    private final MediaMapper mediaMapper;


    @Override
    public void uploadPhotos(Media.UploadPhotosRequest request,
                             StreamObserver<Media.UploadPhotosResponse> responseObserver) {


       List<String> newFileNames =  minioService.uploadFiles(mediaMapper.toPhotoDataDTOs(request.getPhotosList()),mediaMapper.toBucketName(request.getBucketName()));


       responseObserver.onNext(
                Media.UploadPhotosResponse.newBuilder()
                        .addAllFileNames(newFileNames)
                        .build()
        );
        responseObserver.onCompleted();










    }

























}
