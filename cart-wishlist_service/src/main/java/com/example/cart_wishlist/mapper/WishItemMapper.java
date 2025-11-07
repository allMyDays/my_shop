package com.example.cart_wishlist.mapper;

import com.example.cart_wishlist.entity.WishItem;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.dto.wish.rest.WishItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishItemMapper {

    private final ProductGrpcClient productGrpcClient;

    public List<WishItemResponseDTO> mapItems(List<WishItem> items){

        List<WishItemResponseDTO> dtoList = new ArrayList<>();
        for(WishItem item : items){
            WishItemResponseDTO dto = new WishItemResponseDTO();
            dto.setProductId(item.getProductId());
            dtoList.add(dto);

        }


        List<ProductResponseDTO> productDTOs = productGrpcClient.getProductsByIdsFullList(items
                .stream()
                .map(WishItem::getProductId)
                .toList());

        Map<Long, ProductResponseDTO> productMap = productDTOs.stream()
                .collect(Collectors.toMap(ProductResponseDTO::getId, Function.identity()));

        for(WishItemResponseDTO dto : dtoList){

            ProductResponseDTO productDto = productMap.get(dto.getProductId());
            if(productDto != null){
                dto.setTitle(productDto.getTitle());
                dto.setPriceView(productDto.getPriceView());
                dto.setPreviewImageFileName(productDto.getPreviewImageFileName());

            }
        }
        return dtoList;
    }




}
