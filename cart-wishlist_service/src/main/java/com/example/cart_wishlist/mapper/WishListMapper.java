package com.example.cart_wishlist.mapper;

import com.example.cart_wishlist.entity.WishList;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.product.ProductResponseDTO;
import com.example.common.dto.wish.WishItemResponseDTO;
import com.example.common.dto.wish.WishListResponseDTO;
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
    public abstract WishListResponseDTO toWishListDTO(WishList list);



    protected List<WishItemResponseDTO> mapListItems(List<Long> IDs){

        List<WishItemResponseDTO> wishItemResponseDTOS = new ArrayList<>();

        List<ProductResponseDTO> products = productGrpcClient.getProductsByIdsFullList(IDs);

        for(ProductResponseDTO product : products){
            WishItemResponseDTO wishItemResponseDTO = new WishItemResponseDTO();
            wishItemResponseDTO.setProductId(product.getId());
            wishItemResponseDTO.setTitle(product.getTitle());
            wishItemResponseDTO.setPrice(product.getPrice());
            wishItemResponseDTO.setPreviewImageFileName(product.getPreviewImageFileName());

            wishItemResponseDTOS.add(wishItemResponseDTO);
        }
        return wishItemResponseDTOS;

    }












}
