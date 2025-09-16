package com.example.common.client.kafka;

import com.example.common.kafka.dto.product.ProductDeleteImageDto;
import com.example.common.kafka.dto.product.ProductSetImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.common.kafka.constant.Topics.PRODUCT_TOPIC;
import static com.example.common.kafka.constant.keys.ProductKeys.DELETE_PRODUCT_IMAGE;
import static com.example.common.kafka.constant.keys.ProductKeys.SET_PRODUCT_IMAGE;

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

    public void setProductImage(long productId, String imageFileName, boolean previewImage) {
        ProductSetImageDto productSetImageDto = new ProductSetImageDto();
        productSetImageDto.setProductId(productId);
        productSetImageDto.setNewImageFileName(imageFileName);
        productSetImageDto.setPreviewImage(previewImage);

        kafkaTemplate.send(PRODUCT_TOPIC, SET_PRODUCT_IMAGE, productSetImageDto);

    }

    public void deleteProductImage(long productId, String imageFileName, boolean previewImage) {
        ProductDeleteImageDto productDeleteImageDto = new ProductDeleteImageDto();
        productDeleteImageDto.setProductId(productId);
        productDeleteImageDto.setImageFileName(imageFileName);
        productDeleteImageDto.setPreviewImage(previewImage);

        kafkaTemplate.send(PRODUCT_TOPIC, DELETE_PRODUCT_IMAGE, productDeleteImageDto);

    }


}
