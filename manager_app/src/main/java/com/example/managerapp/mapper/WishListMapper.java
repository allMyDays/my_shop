package com.example.managerapp.mapper;

import com.example.managerapp.dto.CartItemDTO;
import com.example.managerapp.dto.WishItemDTO;
import com.example.managerapp.dto.WishListDTO;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.entity.WishList;
import com.example.managerapp.rest.ProductRestClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class WishListMapper {

    @Autowired
    protected ProductRestClient productRestClient;

    @Mapping(target = "itemsDTOList", expression = "java(mapListItems(list.getProductIDs()))")
    @Mapping(target = "totalQuantity", expression = "java(list.getProductIDs().size())")
    public abstract WishListDTO toWishListDTO(WishList list);



    protected List<WishItemDTO> mapListItems(List<Long> IDs){

        List<WishItemDTO> wishItemDTOs = new ArrayList<>();

        List<ProductRecord> products = productRestClient.getProductsByIDs(IDs);

        for(ProductRecord product : products){
            WishItemDTO wishItemDTO = new WishItemDTO();
            wishItemDTO.setProductId(product.id());
            wishItemDTO.setTitle(product.title());
            wishItemDTO.setPrice(product.price());
            wishItemDTO.setPreviewImageFileName(product.previewImageFileName());

            wishItemDTOs.add(wishItemDTO);
        }
        return wishItemDTOs;

    }












}
