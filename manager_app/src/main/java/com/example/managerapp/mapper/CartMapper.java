package com.example.managerapp.mapper;


import com.example.managerapp.dto.CartDTO;
import com.example.managerapp.dto.CartItemDTO;
import com.example.managerapp.entity.Cart;
import com.example.managerapp.entity.CartItem;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.rest.ProductRestClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CartMapper {
    
    @Autowired
    protected ProductRestClient productRestClient;

    @Autowired
    protected CartItemMapper cartItemMapper;
    
    
    
    
    @Mapping(target = "itemsDTOList", expression = "java(mapCartItems(cart.getItems()))")
    @Mapping(target = "totalQuantity", expression = "java(calculateTotalQuantity(cart.getItems()))")
    public abstract CartDTO toCartDTO(Cart cart);

    protected List<CartItemDTO> mapCartItems(List<CartItem> items){
        
        List<CartItemDTO> dtoList = cartItemMapper.toCartItemDTOList(items);

        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductRecord> productRecords = productRestClient.getProductsByIDs(productIds);

        Map<Long, ProductRecord> productMap = productRecords.stream()
                .collect(Collectors.toMap(ProductRecord::id, Function.identity()));

        for(CartItemDTO dto : dtoList){

            ProductRecord productRecord = productMap.get(dto.getProductId());
            if(productRecord != null){
                dto.setTitle(productRecord.title());
                dto.setPrice(productRecord.price());
                dto.setPreviewImageFileName(productRecord.previewImageFileName());

            }
        }
        return dtoList;
    }

    protected int calculateTotalQuantity(List<CartItem> items){
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

    }



}
