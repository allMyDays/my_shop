package com.example.cart_wishlist.service;

import com.example.cart_wishlist.entity.WishList;
import com.example.cart_wishlist.entity.WishItem;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.repository.WishItemRepository;
import com.example.cart_wishlist.repository.WishListRepository;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.exception.ProductNotFoundException;
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




    public WishList getOrCreateWishList(long userId) {

        return listRepository.findById(userId).orElseGet(()->{
                    WishList wishList = new WishList();
                    wishList.setUserId(userId);
                    return listRepository.save(wishList);
                 }

        );
    }

    public void addItem(long userId, long productID){

        if(getListSize(userId)>=8000){
            throw new TooManyItemsException(false);
        }

        if (!productGrpcClient.productExists(productID)){
            throw new ProductNotFoundException(List.of(productID));
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


    public void removeItem(long userId, long productID){
        getOrCreateWishList(userId);

        itemRepository.deleteByUserIdAndProductId(userId, productID);

    }

    public List<WishItem> getItems(long userId, int offset){

        if(offset<0) throw new IllegalArgumentException("offset must be greater or equal to 0");

        getOrCreateWishList(userId);

        int limit = 40;

        Pageable pageable = PageRequest.of(offset/limit, limit);

        return itemRepository.findAllByUserId(userId, pageable);

    }

    public long getListSize(long userId){

        getOrCreateWishList(userId);

        return itemRepository.countQuantityByUserId(userId);
    }

    public List<Long> getProductIdsByUserId(long userId){
        return itemRepository.findProductIdsByUserId(userId);
    }

    public boolean productExists(long productId, long userId){
        return itemRepository.existsByWishListUserIdAndProductId(userId,productId);


    }

}
