package com.example.order_service.controller.rest;

import com.example.order_service.controller.rest.i.IAddressRestController;
import com.example.order_service.exception.AddressNotCorrectException;
import com.example.order_service.service.DeliveryInfoService;
import com.example.order_service.client.GeoapifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequestMapping("/api/order/address")
@RequiredArgsConstructor
public class AddressRestController implements IAddressRestController {

    private final DeliveryInfoService deliveryService;

    private final GeoapifyClient geoapifyClient;

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String query) {
        return geoapifyClient.getAddressSuggestions(query);

    }

    @PostMapping("/set")
    public ResponseEntity<?> setAddress(@RequestBody String address, @AuthenticationPrincipal Jwt jwt) {

        try{
         deliveryService.setDeliveryInfo(getMyUserEntityId(jwt), address);
        }catch (AddressNotCorrectException e){
            return ResponseEntity
                    .status(422)
                    .body(e.getMessage());

        } return ResponseEntity.ok().build();

    }

}
