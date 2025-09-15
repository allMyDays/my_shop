package com.example.common.client.kafka;

import com.example.common.kafka.dto.product.ProductSaveImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import static com.example.common.kafka.constant.Topics.PRODUCT_TOPIC;
import static com.example.common.kafka.constant.keys.ProductKeys.SAVE_PRODUCT_IMAGE;

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

    public void saveProductImage(long creatorId, long productId, String imageFileName, boolean preview) {
        ProductSaveImageDto productSaveImageDto = new ProductSaveImageDto();
        productSaveImageDto.setCreatorId(creatorId);
        productSaveImageDto.setProductId(productId);
        productSaveImageDto.setNewImageFileName(imageFileName);
        productSaveImageDto.setPreview(preview);

        kafkaTemplate.send(PRODUCT_TOPIC, SAVE_PRODUCT_IMAGE, productSaveImageDto);

    }


}
