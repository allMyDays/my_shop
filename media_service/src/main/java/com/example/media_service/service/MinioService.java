package com.example.media_service.service;

import com.example.common.enumeration.media_service.BucketEnum;
import com.example.common.dto.media.kafka.FileDataDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final S3Client s3Client;

    private final RedisAtomicLong redisAtomicLong;

    private final static String fileNameSeparator = "_";

    public MediaType guessContentType(byte[] imageBytes) {
        try {
            return MediaType.parseMediaType(Optional.ofNullable(
                            URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes)))
                    .orElse("application/octet-stream"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public BucketEnum extractBucket(@NonNull String fileName) throws RuntimeException{
        try {
            return BucketEnum.valueOf(fileName.split(fileNameSeparator)[0]);
        }catch (Exception e){
            throw new RuntimeException("cannot find bucket");
        }

    }

    String generateNewFileKey(@NonNull BucketEnum bucket){
        return bucket.name()+fileNameSeparator+redisAtomicLong.incrementAndGet();
    }

    public String uploadFile(FileDataDTO file, BucketEnum bucket) throws Exception {

        String newFileName = generateNewFileKey(bucket);


        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket.name())
                        .key(newFileName)
                        .contentType(file.getContentType())
                .build(), RequestBody.fromBytes(file.getBytes()));

        return newFileName;
    }


    public Map.Entry<byte[], MediaType> getFile(@NonNull String fileName) {

        BucketEnum bucket=extractBucket(fileName);

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest
                .builder()
                .bucket(bucket.name())
                .key(fileName)
                .build());

        byte[] imageBytes = objectBytes.asByteArray();


        return Map.entry(imageBytes,guessContentType(imageBytes));
    }

    public boolean deleteFile(@NonNull String fileName){
        BucketEnum bucket=extractBucket(fileName);

        try{ s3Client.deleteObject(DeleteObjectRequest
                .builder()
                        .bucket(bucket.name())
                        .key(fileName)
                .build());
           return true;

       }catch (Exception e){
           return false;
       }
    }

    public int deleteMultipleFiles(@NonNull List<String> keys) {
        if(keys.isEmpty()||keys.size()>1000){
            return 0;
        }
        try {
            List<ObjectIdentifier> objectsToDelete = keys.stream()
                    .map(key -> ObjectIdentifier.builder()
                            .key(key)
                            .build())
                    .toList();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(extractBucket(keys.get(0)).name())
                    .delete(Delete.builder()
                            .objects(objectsToDelete)
                            .build())
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);

            return response.deleted().size();


        } catch (S3Exception e) {
            throw new RuntimeException("Ошибка S3 при удалении файлов: " + e.awsErrorDetails().errorMessage());
        }
    }

    public List<String> uploadFiles(@NonNull List<FileDataDTO> files, @NonNull BucketEnum bucket) throws Exception {
        List<String> newFileNames = new ArrayList<>();
        for (FileDataDTO file : files) {

            String newFileName = generateNewFileKey(bucket);

            try {
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket.name())
                        .key(newFileName)
                        .contentType(file.getContentType())
                        .build(), RequestBody.fromBytes(file.getBytes()));

                newFileNames.add(newFileName);

            } catch (Exception e) {
                // Лог ошибки и продолжить
            }
        } return newFileNames;
    }


}
