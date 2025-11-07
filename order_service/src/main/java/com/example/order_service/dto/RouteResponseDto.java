package com.example.order_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteResponseDto {
    private List<Feature> features;
    private Properties properties; // поле на верхнем уровне
    private String type;

    @Data
    public static class Feature {
        private String type;
        private Properties properties;
        private Geometry geometry;
    }

    @Data
    public static class Properties {
        private String mode;
        private String units;
        private long distance;
        private long time;
        private List<Waypoint> waypoints;
    }

    @Data
    public static class Waypoint {
        private double lat;
        private double lon;
    }

    @Data
    public static class Geometry {
        private String type;
        private List<List<List<Double>>> coordinates;
    }











  /*  private List<Feature> features;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private Properties properties;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private double distance;
        private double time;

    }*/


}
