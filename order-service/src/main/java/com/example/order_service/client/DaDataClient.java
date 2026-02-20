package com.example.order_service.client;

import com.example.common.dto.user.rest.UserAddressDto;
import com.example.order_service.entity.DeliveryInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DaDataClient {

   /* @Value("${dadata.token}")
    private String apiToken;

    @Value("${dadata.url}")
    private String url;

    @Value("${dadata.secret-key}")
    private String secretKey;


    private final RestTemplate restTemplate = new RestTemplate();

    ObjectMapper objectMapper = new ObjectMapper();



    public List<String> suggestAddress(@NonNull String query) {
        query=query.trim();

        HttpHeaders headers = buildHeaders();
        String body = "{ \"query\": \"" + query + "\", \"count\": 5 }";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        List<String> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response.getBody()).get("suggestions");
            for (JsonNode node : root) {
                results.add(node.get("value").asText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public Optional <UserAddressDto> validateAndGetAddressInfo(@NonNull String inputAddress) {
        inputAddress=inputAddress.trim();
        try {
            HttpHeaders headers = buildHeaders();

            String body = "{ \"query\": \"" + inputAddress + "\", \"count\": 1 }";

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode suggestions = root.get("suggestions");

            if (suggestions == null || suggestions.isEmpty()) {
                return Optional.empty();
            }

            JsonNode first = suggestions.get(0);
            JsonNode dataNode = first.get("data");


            int qcComplete = dataNode.get("qc_complete").asInt();
            String street = dataNode.get("street").asText();
            String house = dataNode.get("house").asText();

            if (qcComplete != 0||street.equals("null")||house.equals("null")) {
                return Optional.empty();
            }

            String fullAddress = first.get("value").asText();
            String latStr = dataNode.get("geo_lat").asText();
            String lonStr = dataNode.get("geo_lon").asText();

            if(latStr.equals("null")||lonStr.equals("null")) {
                return Optional.empty();
            }

            UserAddressDto addressDto = new UserAddressDto();
            addressDto.setFullAddress(fullAddress);
            addressDto.setLatitude(Double.parseDouble(latStr));
            addressDto.setLongitude(Double.parseDouble(lonStr));

            return Optional.of(addressDto);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + apiToken);
        headers.set("X-Secret", secretKey);
        return headers;
    }*/

}
