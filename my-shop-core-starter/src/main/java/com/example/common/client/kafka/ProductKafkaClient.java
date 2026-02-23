package com.example.common.client.kafka;

import com.example.common.dto.product.kafka.ProductDeleteImageDto;
import com.example.common.dto.product.kafka.ProductSetImageDto;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.common.constant.kafka.Topics.PRODUCT_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.ProductKeys.DELETE_PRODUCT_IMAGE;
import static com.example.common.constant.kafka.keys.ProductKeys.SET_PRODUCT_IMAGE;

@Service
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class ProductKafkaClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Lazy
    public void setKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void setProductImage(long productId, @NonNull String imageFileName, boolean previewImage) {
        ProductSetImageDto productSetImageDto = new ProductSetImageDto();
        productSetImageDto.setProductId(productId);
        productSetImageDto.setNewImageFileName(imageFileName);
        productSetImageDto.setPreviewImage(previewImage);

        kafkaTemplate.send(PRODUCT_REQUEST_TOPIC, SET_PRODUCT_IMAGE, productSetImageDto);

    }

    public void deleteProductImage(long productId, @NonNull String imageFileName, boolean previewImage) {
        ProductDeleteImageDto productDeleteImageDto = new ProductDeleteImageDto();
        productDeleteImageDto.setProductId(productId);
        productDeleteImageDto.setImageFileName(imageFileName);
        productDeleteImageDto.setPreviewImage(previewImage);

        kafkaTemplate.send(PRODUCT_REQUEST_TOPIC, DELETE_PRODUCT_IMAGE, productDeleteImageDto);

    }


}
