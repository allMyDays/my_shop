package com.example.cart_wishlist.service;

import com.example.cart_wishlist.entity.WishList;
import com.example.cart_wishlist.entity.WishItem;
import com.example.cart_wishlist.repository.WishItemRepository;
import com.example.cart_wishlist.repository.WishListRepository;
import com.example.common.client.grpc.ProductGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository listRepository;

    private final WishItemRepository itemRepository;

    private final ProductGrpcClient productGrpcClient;




    public WishList getOrCreateWishList(Long userId) {

        return listRepository.findById(userId).orElseGet(()->{
                    WishList wishList = new WishList();
                    wishList.setUserId(userId);
                    return listRepository.save(wishList);
                 }

        );
    }

    public void addItem(Long userId, Long productID){

        if (!productGrpcClient.productExists(productID)){
            return;
        }
        WishList wishList = getOrCreateWishList(userId);

        Optional<WishItem> wishListItemOptional = itemRepository.findByWishListUserIdAndProductId(userId, productID);
        if (wishListItemOptional.isEmpty()){
            WishItem wishItem = new WishItem();
            wishItem.setProductId(productID);
            wishItem.setWishList(wishList);
            itemRepository.save(wishItem);
        }

    }


    public void removeItem(Long userId, Long productID){
        getOrCreateWishList(userId);

        itemRepository.deleteByUserIdAndProductId(userId, productID);

    }

    public List<WishItem> getItems(Long userId, int offset){

        if(offset<0) throw new IllegalArgumentException("offset must be greater than 0");

        getOrCreateWishList(userId);

        int limit = 40;

        Pageable pageable = PageRequest.of(offset/limit, limit);

        return itemRepository.findAllByUserId(userId, pageable);

    }

    public long getListSize(Long userId){

        getOrCreateWishList(userId);

        return itemRepository.countQuantityByUserId(userId);
    }

    public List<Long> getProductIdsByUserId(Long userId){
        return itemRepository.findProductIdsByUserId(userId);
    }








}
