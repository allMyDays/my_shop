package com.example.managerapp.service;

import com.example.managerapp.entity.enums.MinIO_bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class MinioService {

    private final S3Client s3;

    public MinioService(S3Client s3) {
        this.s3 = s3;
    }

    public Long saveFile(MultipartFile file, MinIO_bucket bucket) throws IOException {

        Long fileID = 0l;

        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket.name())
                        .key(fileID.toString())
                        .contentType(file.getContentType())
                .build(), RequestBody.fromBytes(file.getBytes()));

        return fileID;
    }

    public byte[] getFile(String fileName, MinIO_bucket bucket) throws IOException {

        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(GetObjectRequest
                .builder()
                        .bucket(bucket.name())
                        .key(fileName)
                .build());
        return objectBytes.asByteArray();

    }









}
