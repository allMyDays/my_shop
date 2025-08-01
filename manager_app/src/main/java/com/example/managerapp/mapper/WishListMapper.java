package com.example.managerapp.mapper;

import com.example.managerapp.client.grpc.ProductGrpcClient;
import com.example.managerapp.dto.product.ProductResponseDTO;
import com.example.managerapp.dto.wish.WishItemDTO;
import com.example.managerapp.dto.wish.WishListDTO;
import com.example.managerapp.entity.WishList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class WishListMapper {

    @Autowired
    protected ProductGrpcClient productGrpcClient;

    @Mapping(target = "itemsDTOList", expression = "java(mapListItems(list.getProductIDs()))")
    @Mapping(target = "totalQuantity", expression = "java(list.getProductIDs().size())")
    public abstract WishListDTO toWishListDTO(WishList list);



    protected List<WishItemDTO> mapListItems(List<Long> IDs){

        List<WishItemDTO> wishItemDTOs = new ArrayList<>();

        List<ProductResponseDTO> products = productGrpcClient.getProductsByIdsFullList(IDs);

        for(ProductResponseDTO product : products){
            WishItemDTO wishItemDTO = new WishItemDTO();
            wishItemDTO.setProductId(product.getId());
            wishItemDTO.setTitle(product.getTitle());
            wishItemDTO.setPrice(product.getPrice());
            wishItemDTO.setPreviewImageFileName(product.getPreviewImageFileName());

            wishItemDTOs.add(wishItemDTO);
        }
        return wishItemDTOs;

    }












}
