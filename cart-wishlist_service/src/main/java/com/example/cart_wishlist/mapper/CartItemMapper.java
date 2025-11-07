package com.example.cart_wishlist.mapper;

import com.example.cart_wishlist.entity.CartItem;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import com.example.common.dto.product.rest.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.common.service.CommonProductService.formatPrice;

@Service
@RequiredArgsConstructor
public class CartItemMapper {

    private final ProductGrpcClient productGrpcClient;

    public List<CartItemResponseDTO> mapCartItems(List<CartItem> items){

        List<CartItemResponseDTO> dtoList = new ArrayList<>();
        for(CartItem item : items){
            CartItemResponseDTO dto = new CartItemResponseDTO();
            dto.setId(item.getId());
            dto.setProductId(item.getProductId());
            dto.setQuantity(item.getQuantity());
            dtoList.add(dto);

        }

        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductResponseDTO> productDTOs = productGrpcClient.getProductsByIdsFullList(productIds);

        Map<Long, ProductResponseDTO> productMap = productDTOs.stream()
                .collect(Collectors.toMap(ProductResponseDTO::getId, Function.identity()));

        for(CartItemResponseDTO dto : dtoList){

            ProductResponseDTO productDto = productMap.get(dto.getProductId());
            if(productDto != null){
                dto.setTitle(productDto.getTitle());
                dto.setPricePerProductInt(productDto.getPriceInt());
                dto.setTotalPriceView(formatPrice(productDto.getPriceInt()*dto.getQuantity()));
                dto.setPreviewImageFileName(productDto.getPreviewImageFileName());

            }
        }
        return dtoList;
    }




}
