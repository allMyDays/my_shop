package com.example.order_service.client;

import com.example.common.dto.user.rest.UserAddressDto;
import com.example.order_service.dto.RouteResponseDto;
import com.example.order_service.exception.AddressNotCorrectException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GeoapifyClient {

    @Value("${geoapify.api.key}")
    private String apiKey;

    @Value("${geoapify.routing.base-url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    public RouteResponseDto calculateRoute(double lat1, double lon1, double lat2, double lon2) {


        String url = apiBaseUrl
                +"/routing"
                + "?waypoints=" + lat1 + "," + lon1
                + "|" + lat2 + "," + lon2
                + "&mode=drive"
                + "&apiKey=" + apiKey;


        System.out.println("URL: " + url);
        RouteResponseDto response=null;
        try {
             response = restTemplate.getForObject(url, RouteResponseDto.class);
            return response;
        } catch (HttpClientErrorException e) {
            String httpStatus = e.getStatusCode().toString();
            String responseBody = e.getResponseBodyAsString();
            System.out.println(httpStatus);
            System.out.println(responseBody);
        } catch (Exception e) {
            log.warn("Error while doing method testRoute: {}",e.getMessage());
            e.printStackTrace();
        } return response;
    }

    public List<String> getAddressSuggestions(String query) {
        if(query==null||query.trim().isEmpty()) return new ArrayList<>();

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);


        String url = apiBaseUrl+
                "/geocode/autocomplete" +
                "?text=" + encodedQuery +
                "&filter=countrycode:ru"+
                "&limit=10"+
                "&apiKey=" + apiKey;

        try {
            String response = restTemplate.getForObject(new URI(url), String.class);
            List<String> suggestions = new ArrayList<>();
            JSONObject root = new JSONObject(response);
            JSONArray features = root.optJSONArray("features");

            if (features != null) {
                for (int i = 0; i < features.length(); i++) {
                    JSONObject feature = features.getJSONObject(i);
                    JSONObject properties = feature.optJSONObject("properties");
                    if (properties != null) {
                        String formatted = properties.optString("formatted");
                        if (formatted != null && !formatted.isEmpty()) {
                            suggestions.add(formatted);
                        }
                    }
                }
            }
            return suggestions;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении подсказок от Geoapify: " + e.getMessage(), e);
        }
    }

    public UserAddressDto normalizeAddress(String address) {
        String url = apiBaseUrl+
                "/geocode/search" +
                "?text=" + URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8) +
                "&apiKey=" + apiKey;

        try {
            String response = restTemplate.getForObject(new URI(url), String.class);
            JSONObject root = new JSONObject(response);
            JSONArray features = root.optJSONArray("features");

            if (features==null||features.isEmpty()) {
                throw new AddressNotCorrectException();
            }

            JSONObject firstResult = features.getJSONObject(0).getJSONObject("properties");

            // Проверяем наличие ключевых компонентов
            boolean hasCity = firstResult.has("city") || firstResult.has("town") || firstResult.has("village");
            boolean hasStreet = firstResult.has("street");
            boolean hasHousenumber = firstResult.has("housenumber");

            if (!hasCity||!hasStreet||!hasHousenumber) {
                throw new AddressNotCorrectException();
            }

            return new UserAddressDto(
                    firstResult.getString("formatted"),
                    firstResult.getDouble("lat"),
                    firstResult.getDouble("lon")
            );

        } catch (AddressNotCorrectException e) {
            throw e; // Перебрасываем своё исключение
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при нормализации адреса: " + e.getMessage(), e);
        }
    }




}
