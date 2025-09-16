package com.example.media_service.service.image;


import com.example.common.client.kafka.ProductKafkaClient;
import com.example.common.client.kafka.UserKafkaClient;
import com.example.common.exception.UserNotFoundException;
import com.example.media_service.entity.enumeration.MinIO_bucket;
import com.example.media_service.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

private final MinioService minioService;

private final UserKafkaClient userKafkaClient;

private final ProductKafkaClient productKafkaClient;


public void saveUserAvatar(MultipartFile file, Long userId) throws Exception {

    deleteUserAvatar(false,userId);

    String newFileName =  minioService.saveFile(userId,file, MinIO_bucket.users);

    userKafkaClient.updateUserAvatar(userId,newFileName);

}

public void deleteUserAvatar(boolean sendToKafka, Long userId)  {

    List<String> tempKeys = minioService.findByKeyFirstPart(userId,MinIO_bucket.users);
    if(!tempKeys.isEmpty()){
        minioService.deleteFile(tempKeys.getFirst(), MinIO_bucket.users);
    }
    if(sendToKafka){
        userKafkaClient.deleteUserAvatar(userId);
    }
}

    public Map.Entry<byte[],MediaType> getUserAvatar(String fileName) throws IOException {

        byte[] imageBytes = minioService.getFile(fileName, MinIO_bucket.users);

        return Map.entry(imageBytes,guessContentType(imageBytes));
    }



public Map.Entry<byte[],MediaType> getProductImage(String fileName) throws IOException {

    byte[] imageBytes = minioService.getFile(fileName, MinIO_bucket.products);

    return Map.entry(imageBytes,guessContentType(imageBytes));
}

public void saveProductImage(Long productId, MultipartFile file, boolean previewImage, Long adminId) {
    String newFileName;
    try {
       newFileName = minioService.saveFile(productId,file, MinIO_bucket.products);
    } catch (Exception e) {
        //todo;
        return;
    } if(previewImage){
        boolean deleted = minioService.deleteFile(newFileName, MinIO_bucket.products);
        if(!deleted){
            //todo
        }
    } productKafkaClient.setProductImage(productId,newFileName,previewImage);

}

    public void deleteProductImage(Long productId, String fileName, boolean previewImage, Long adminId){
             boolean deleted = minioService.deleteFile(fileName, MinIO_bucket.products);
             if(!deleted){
                 //todo
                 return;
             }
             productKafkaClient.deleteProductImage(productId,fileName,previewImage);
    }


public MediaType guessContentType(byte[] imageBytes) {
    try {
        return MediaType.parseMediaType(Optional.ofNullable(
                        URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes)))
                .orElse("application/octet-stream"));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }


}






















}
