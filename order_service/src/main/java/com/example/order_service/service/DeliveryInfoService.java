package com.example.order_service.service;

import com.example.common.dto.user.rest.UserAddressDto;
import com.example.order_service.client.DaDataClient;
import com.example.order_service.dto.RouteResponseDto;
import com.example.order_service.entity.DeliveryInfo;
import com.example.order_service.exception.AddressNotCorrectException;
import com.example.order_service.repository.DeliveryInfoRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryInfoService {

    private final DeliveryInfoRepository deliveryRepository;

    private final DaDataClient daDataClient;

    public final static String STORAGE_ADDRESS = "г. Москва, Профсоюзная улица, 92";

    private final static double STORAGE_LATITUDE=55.649108;

    private final static double STORAGE_LONGITUDE=37.528433;

    private final RoutingService routingService;


    public Optional<DeliveryInfo> getDeliveryInfo(long userId) {

        return deliveryRepository.findById(userId);

    }
    public void setDeliveryInfo(long userId, @NonNull String fullAddress){

        Optional<UserAddressDto> newAddressOptional = daDataClient.validateAndGetAddressInfo(fullAddress);

        if(newAddressOptional.isEmpty()){
            throw new AddressNotCorrectException();
        }
        UserAddressDto newAddress = newAddressOptional.get();

        Optional<DeliveryInfo> existingDeliveryInfoOpt = deliveryRepository.findById(userId);

        if(existingDeliveryInfoOpt.isPresent()){
            if(existingDeliveryInfoOpt.get().getUserAddress().equals(newAddress.getFullAddress())){
                return;
            }
        }

        DeliveryInfo newDeliveryInfo = new DeliveryInfo();
        newDeliveryInfo.setUserId(userId);
        newDeliveryInfo.setUserAddress(newAddress.getFullAddress());
        newDeliveryInfo.setAddressLongitude(newAddress.getLongitude());
        newDeliveryInfo.setAddressLatitude(newAddress.getLatitude());

        RouteResponseDto routeResponse = routingService.testRoute(newAddress.getLatitude(),newAddress.getLongitude(), STORAGE_LATITUDE, STORAGE_LONGITUDE);

        RouteResponseDto.Properties properties = routeResponse.getFeatures().get(0).getProperties();

        newDeliveryInfo.setDeliveryDistance(properties.getDistance());
        newDeliveryInfo.setDeliveryTime(properties.getTime());
        deliveryRepository.save(newDeliveryInfo);


    }





    public static long calculateStraightDistance(double lat1, double lon1) {
        final int R = 6371; // Земной радиус в км

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(STORAGE_LATITUDE);
        double deltaLat = Math.toRadians(STORAGE_LATITUDE - lat1);
        double deltaLon = Math.toRadians(STORAGE_LONGITUDE - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (long)(R * c * 1000);
    }

    public static String formatDistance(long meters) {
        if (meters < 1000) {
            return meters + " " + getMeterWord(meters);
        }

        double km = meters / 1000.0;
        long wholeKm = (long) km;

        DecimalFormat df;
        if (km == wholeKm) {
            // Целые километры
            df = new DecimalFormat("#,###");
        } else {
            // Дробные километры с одной цифрой после запятой
            df = new DecimalFormat("#,##0.#");
        }
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("ru", "RU")));
        df.setGroupingUsed(true);

        String kmWord = getKilometerWord(wholeKm);

        return df.format(km) + " " + kmWord;
    }

    // --- окончания ---
    private static String getMeterWord(long n) {
        n = n % 100;
        if (n >= 11 && n <= 19) return "метров";
        switch ((int)(n % 10)) {
            case 1: return "метр";
            case 2:
            case 3:
            case 4: return "метра";
            default: return "метров";
        }
    }

    private static String getKilometerWord(long n) {
        n = n % 100;
        if (n >= 11 && n <= 19) return "километров";
        switch ((int)(n % 10)) {
            case 1: return "километр";
            case 2:
            case 3:
            case 4: return "километра";
            default: return "километров";
        }
    }




    public static String formatDuration(long seconds) {
        final long SECONDS_IN_HOUR = 3600;
        final long SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
        final long SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;
        final long SECONDS_IN_MONTH = SECONDS_IN_DAY * 30; // усреднённо 30 дней

        if (seconds < SECONDS_IN_HOUR) {
            return "меньше часа";
        }

        long months = seconds / SECONDS_IN_MONTH;
        seconds %= SECONDS_IN_MONTH;

        long weeks = seconds / SECONDS_IN_WEEK;
        seconds %= SECONDS_IN_WEEK;

        long days = seconds / SECONDS_IN_DAY;
        seconds %= SECONDS_IN_DAY;

        long hours = seconds / SECONDS_IN_HOUR;

        StringBuilder sb = new StringBuilder();

        if (months > 0) sb.append(months).append(" ").append(getMonthWord(months)).append(", ");
        if (weeks > 0) sb.append(weeks).append(" ").append(getWeekWord(weeks)).append(", ");
        if (days > 0) sb.append(days).append(" ").append(getDayWord(days)).append(", ");
        if (hours > 0) sb.append(hours).append(" ").append(getHourWord(hours));

        if (sb.toString().endsWith(", ")) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }

    // --- Вспомогательные методы для окончаний ---
    private static String getMonthWord(long n) {
        n = n % 100;
        if (n >= 11 && n <= 19) return "месяцев";
        return switch ((int) (n % 10)) {
            case 1 -> "месяц";
            case 2, 3, 4 -> "месяца";
            default -> "месяцев";
        };
    }

    private static String getWeekWord(long n) {
        n = n % 100;
        if (n >= 11 && n <= 19) return "недель";
        return switch ((int) (n % 10)) {
            case 1 -> "неделя";
            case 2, 3, 4 -> "недели";
            default -> "недель";
        };
    }

    private static String getDayWord(long n) {
        n = n % 100;
        if (n >= 11 && n <= 19) return "дней";
        return switch ((int) (n % 10)) {
            case 1 -> "день";
            case 2, 3, 4 -> "дня";
            default -> "дней";
        };
    }

    private static String getHourWord(long n) {
        n = n % 100;
        if (n >= 11 && n <= 19) return "часов";
        return switch ((int) (n % 10)) {
            case 1 -> "час";
            case 2, 3, 4 -> "часа";
            default -> "часов";
        };
    }



    public static int calculateDeliveryPrice(long distanceMeters, long timeSeconds) {

        int minPrice = 200;

        double freeKm = 2.0;

        double pricePerKm = 18.0;

        double pricePerMinute = 2.5;

        double distanceKm = distanceMeters / 1000.0;
        double timeMinutes = timeSeconds / 60.0;

        double paidKm = Math.max(0, distanceKm - freeKm);

        double total = minPrice + (paidKm * pricePerKm) + (timeMinutes * pricePerMinute);

        return (int) Math.round(total);
    }





}
