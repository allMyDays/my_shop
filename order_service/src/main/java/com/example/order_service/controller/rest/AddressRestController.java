package com.example.order_service.controller.rest;

import com.example.order_service.client.DaDataClient;
import com.example.order_service.controller.rest.i.IAddressRestController;
import com.example.order_service.exception.AddressNotCorrectException;
import com.example.order_service.service.DeliveryInfoService;
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

    private final DaDataClient daDataClient;

    private final DeliveryInfoService deliveryService;

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String query) {
        return daDataClient.suggestAddress(query);

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
