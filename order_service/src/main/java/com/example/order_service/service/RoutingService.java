package com.example.order_service.service;

import com.example.order_service.dto.RouteResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RoutingService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    @Value("${geoapify.routing.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public RouteResponseDto testRoute(double lat1, double lon1, double lat2, double lon2) {


        String url = apiUrl
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
            e.printStackTrace();
        } return response;
    }
}
