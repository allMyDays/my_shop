package com.example.media_service.service;

import com.example.media_service.entity.enumeration.MinIO_bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final S3Client s3;

    private final RedisAtomicLong redisAtomicLong;


    public String saveFile(Long keyFirstPart, MultipartFile file, MinIO_bucket bucket) throws Exception {

        String newFileName = keyFirstPart+":"+redisAtomicLong.incrementAndGet();


        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket.name())
                        .key(newFileName)
                        .contentType(file.getContentType())
                .build(), RequestBody.fromBytes(file.getBytes()));

        return newFileName;
    }

    public byte[] getFile(String fileName, MinIO_bucket bucket) {

        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(GetObjectRequest
                  .builder()
                          .bucket(bucket.name())
                          .key(fileName)
                  .build());
        return objectBytes.asByteArray();

    }

    public List<String> findByKeyFirstPart(Long keyFirstPart, MinIO_bucket bucket) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket.name())
                .prefix(keyFirstPart.toString()+":")
                .delimiter("/") // файлы не в подпапках
                .build();

        ListObjectsV2Response response = s3.listObjectsV2(request);

        return response.contents().stream().map(S3Object::key).toList();
    }




    public boolean deleteFile(String fileName, MinIO_bucket bucket){

       try{ s3.deleteObject(DeleteObjectRequest
                .builder()
                        .bucket(bucket.name())
                        .key(fileName)
                .build());
           return true;

       }catch (Exception e){
           return false;
       }
    }









}
