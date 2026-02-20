package com.example.order_service.unit.service;

import com.example.common.dto.user.rest.UserAddressDto;
import com.example.order_service.dto.RouteResponseDto;
import com.example.order_service.entity.DeliveryInfo;
import com.example.order_service.exception.AddressNotCorrectException;
import com.example.order_service.repository.DeliveryInfoRepository;
import com.example.order_service.service.DeliveryInfoService;
import com.example.order_service.client.GeoapifyClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryInfoServiceUnitTest {

    @Mock
    private DeliveryInfoRepository deliveryRepository;

  //  @Mock
  //  private DaDataClient daDataClient;

    @Mock
    private GeoapifyClient geoapifyClient;

    @InjectMocks
    private DeliveryInfoService deliveryInfoService;

    private final Long USER_ID = 1L;
    private final String VALID_ADDRESS = "г. Москва, ул. Ленина, 1";

    @Test
    void getDeliveryInfo_WhenExists_ReturnsDeliveryInfo() {
        // Given
        DeliveryInfo expectedInfo = new DeliveryInfo();
        expectedInfo.setUserId(USER_ID);
        when(deliveryRepository.findById(USER_ID)).thenReturn(Optional.of(expectedInfo));

        // When
        Optional<DeliveryInfo> result = deliveryInfoService.getDeliveryInfo(USER_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedInfo, result.get());
        verify(deliveryRepository).findById(USER_ID);
    }

    @Test
    void getDeliveryInfo_WhenNotExists_ReturnsEmpty() {
        // Given
        when(deliveryRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When
        Optional<DeliveryInfo> result = deliveryInfoService.getDeliveryInfo(USER_ID);

        // Then
        assertFalse(result.isPresent());
        verify(deliveryRepository).findById(USER_ID);
    }

    @Test
    void setDeliveryInfo_WithInvalidAddress_ThrowsAddressNotCorrectException() {
        // Given
        String invalidAddress = "Invalid Address";
        when(geoapifyClient.normalizeAddress(invalidAddress)).thenThrow(AddressNotCorrectException.class);

        // When & Then
        assertThrows(AddressNotCorrectException.class,
                () -> deliveryInfoService.setDeliveryInfo(USER_ID, invalidAddress));

        verify(geoapifyClient).normalizeAddress(invalidAddress);
        verify(deliveryRepository, never()).save(any());
    }


    @Test
    void setDeliveryInfo_WithNewAddress_CreatesNewDeliveryInfo() {
        // Given
        String newAddress = "г. Москва, ул. Новая, 10";
        UserAddressDto userAddressDto = new UserAddressDto();
        userAddressDto.setFullAddress(newAddress);
        userAddressDto.setLatitude(55.7558);
        userAddressDto.setLongitude(37.6173);

        RouteResponseDto routeResponse = createMockRouteResponse(15000, 1800); // 15km, 30min
        DeliveryInfo savedInfo = new DeliveryInfo();

        when(geoapifyClient.normalizeAddress(newAddress)).thenReturn(userAddressDto);
        when(deliveryRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(geoapifyClient.calculateRoute(eq(55.7558), eq(37.6173), eq(55.649108), eq(37.528433)))
                .thenReturn(routeResponse);
        when(deliveryRepository.save(any(DeliveryInfo.class))).thenReturn(savedInfo);

        // When
        deliveryInfoService.setDeliveryInfo(USER_ID, newAddress);

        // Then
        verify(geoapifyClient).normalizeAddress(newAddress);
        verify(geoapifyClient).calculateRoute(55.7558, 37.6173, 55.649108, 37.528433);
        verify(deliveryRepository).save(argThat(info ->
                info.getUserId().equals(USER_ID) &&
                        info.getUserAddress().equals(newAddress) &&
                        info.getDeliveryDistance() == 15000 &&
                        info.getDeliveryTime() == 1800
        ));
    }

    @Test
    void setDeliveryInfo_WithDifferentExistingAddress_UpdatesDeliveryInfo() {
        // Given
        String newAddress = "г. Москва, ул. Новая, 10";
        String oldAddress = "г. Москва, ул. Старая, 5";

        UserAddressDto userAddressDto = new UserAddressDto();
        userAddressDto.setFullAddress(newAddress);
        userAddressDto.setLatitude(55.7558);
        userAddressDto.setLongitude(37.6173);

        DeliveryInfo existingInfo = new DeliveryInfo();
        existingInfo.setUserId(USER_ID);
        existingInfo.setUserAddress(oldAddress);

        RouteResponseDto routeResponse = createMockRouteResponse(12000, 1500);

        when(geoapifyClient.normalizeAddress(newAddress)).thenReturn(userAddressDto);
        when(deliveryRepository.findById(USER_ID)).thenReturn(Optional.of(existingInfo));
        when(geoapifyClient.calculateRoute(eq(55.7558), eq(37.6173), eq(55.649108), eq(37.528433)))
                .thenReturn(routeResponse);
        when(deliveryRepository.save(any(DeliveryInfo.class))).thenReturn(existingInfo);

        // When
        deliveryInfoService.setDeliveryInfo(USER_ID, newAddress);

        // Then
        verify(deliveryRepository).save(argThat(info ->
                info.getUserAddress().equals(newAddress)
        ));
    }

    @ParameterizedTest
    @CsvSource({
            "55.649108, 37.528433, 0",    // Same as storage
            "55.7539, 37.6208, 13010",     // Moscow center
            "55.7558, 37.6173, 13105"      // Another Moscow point
    })
    void calculateStraightDistance_VariousLocations_ReturnsCorrectDistance(double lat, double lon, long expectedDistance) {
        // When
        long result = DeliveryInfoService.calculateStraightDistance(lat, lon);

        // Then - allow some tolerance for floating point calculations
        assertEquals(expectedDistance, result, 10);
    }

    @ParameterizedTest
    @CsvSource({
            "0, '0 метров'",
            "1, '1 метр'",
            "2, '2 метра'",
            "5, '5 метров'",
            "11, '11 метров'",
            "21, '21 метр'",
            "22, '22 метра'",
            "25, '25 метров'",
            "100, '100 метров'",
            "999, '999 метров'",
            "1000, '1 километр'",
            "1500, '1,5 километр'",
            "2000, '2 километра'",
            "2500, '2,5 километра'",
            "5000, '5 километров'",
            "10000, '10 километров'",
            "21500, '21,5 километр'",
            "100000, '100 километров'",
    })
    void formatDistance_VariousDistances_ReturnsCorrectFormat(long meters, String expected) {
        // When
        String result = DeliveryInfoService.formatDistance(meters);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 'меньше часа'",
            "3599, 'меньше часа'",
            "3600, '1 час'",
            "7200, '2 часа'",
            "10800, '3 часа'",
            "18000, '5 часов'",
            "86400, '1 день'",
            "172800, '2 дня'",
            "259200, '3 дня'",
            "432000, '5 дней'",
            "604800, '1 неделя'",
            "1209600, '2 недели'",


            "1814400, '3 недели'",
            "2419200, '4 недели'",
            "2592000, '1 месяц'",
            "5184000, '2 месяца'",
            "7776000, '3 месяца'",
            "10368000, '4 месяца'",
            "12960000, '5 месяцев'",
            "3600, '1 час'",
            "90000, '1 день, 1 час'",
            "950400, '1 неделя, 4 дня'",
            "2764800, '1 месяц, 2 дня'"
    })
    void formatDuration_VariousDurations_ReturnsCorrectFormat(long seconds, String expected) {
        // When
        String result = DeliveryInfoService.formatDuration(seconds);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 200",           // Min price
            "1000, 600, 225",      // Within free km
            "2000, 600, 225",      // Exactly free km
            "3000, 600, 243",      // 1km paid
            "5000, 1200, 304",     // 3km paid, 20min
            "10000, 1800, 419",    // 8km paid, 30min
            "20000, 3600, 674"     // 18km paid, 60min
    })
    void calculateDeliveryPrice_VariousDistancesAndTimes_ReturnsCorrectPrice(long distanceMeters, long timeSeconds, int expectedPrice) {
        // When
        int result = DeliveryInfoService.calculateDeliveryPrice(distanceMeters, timeSeconds);

        // Then
        assertEquals(expectedPrice, result);
    }


    @ParameterizedTest
    @ValueSource(longs = {1, 21, 31, 41, 51, 61, 71, 81, 91, 101})
    void getMeterWord_SingularForms_ReturnsCorrectWord(long n) {
        // When
        String result = DeliveryInfoServiceUnitTest.getMeterWord(n);

        // Then
        assertEquals("метр", result);
    }

    @ParameterizedTest
    @ValueSource(longs = {2, 3, 4, 22, 23, 24, 32, 33, 34})
    void getMeterWord_PluralForms1_ReturnsCorrectWord(long n) {
        // When
        String result = DeliveryInfoServiceUnitTest.getMeterWord(n);

        // Then
        assertEquals("метра", result);
    }

    @ParameterizedTest
    @ValueSource(longs = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 25, 30})
    void getMeterWord_PluralForms2_ReturnsCorrectWord(long n) {
        // When
        String result = DeliveryInfoServiceUnitTest.getMeterWord(n);

        // Then
        assertEquals("метров", result);
    }

    // Helper method to access private static method via reflection for testing
    private static String getMeterWord(long n) {
        try {
            var method = DeliveryInfoService.class.getDeclaredMethod("getMeterWord", long.class);
            method.setAccessible(true);
            return (String) method.invoke(null, n);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to create mock route response
    private RouteResponseDto createMockRouteResponse(long distance, long time) {
        RouteResponseDto routeResponse = new RouteResponseDto();
        RouteResponseDto.Feature feature = new RouteResponseDto.Feature();
        RouteResponseDto.Properties properties = new RouteResponseDto.Properties();
        properties.setDistance(distance);
        properties.setTime(time);
        feature.setProperties(properties);
        routeResponse.setFeatures(List.of(feature));
        return routeResponse;
    }
}