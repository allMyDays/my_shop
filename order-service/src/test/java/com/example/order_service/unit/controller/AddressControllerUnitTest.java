package com.example.order_service.unit.controller;

import com.example.order_service.client.DaDataClient;
import com.example.order_service.client.GeoapifyClient;
import com.example.order_service.controller.rest.AddressRestController;
import com.example.order_service.exception.AddressNotCorrectException;
import com.example.order_service.service.DeliveryInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressControllerUnitTest {

    @Mock
    private GeoapifyClient geoapifyClient;

    @Mock
    private DeliveryInfoService deliveryService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private AddressRestController addressController;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_ADDRESS = "Москва, Тверская ул., 1";

    // Tests for suggest method
    @Test
    void suggest_WhenValidQuery_ShouldReturnAddressSuggestions() {
        // Arrange
        String query = "Москва, Тверская";
        List<String> expectedSuggestions = List.of(
                "Москва, Тверская ул., 1",
                "Москва, Тверская ул., 2",
                "Москва, Тверская ул., 3"
        );

        when(geoapifyClient.getAddressSuggestions(eq(query))).thenReturn(expectedSuggestions);

        // Act
        List<String> result = addressController.suggest(query);

        // Assert
        assertEquals(expectedSuggestions, result);
        verify(geoapifyClient).getAddressSuggestions(query);
    }

    @Test
    void suggest_WhenEmptyQuery_ShouldReturnEmptyList() {
        // Arrange
        String emptyQuery = "";
        List<String> emptySuggestions = List.of();

        when(geoapifyClient.getAddressSuggestions(eq(emptyQuery))).thenReturn(emptySuggestions);

        // Act
        List<String> result = addressController.suggest(emptyQuery);

        // Assert
        assertTrue(result.isEmpty());
        verify(geoapifyClient).getAddressSuggestions(emptyQuery);
    }

    @Test
    void suggest_WhenNullQuery_ShouldHandleGracefully() {
        // Arrange
        List<String> emptySuggestions = List.of();

        when(geoapifyClient.getAddressSuggestions(isNull())).thenReturn(emptySuggestions);

        // Act
        List<String> result = addressController.suggest(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(geoapifyClient).getAddressSuggestions(null);
    }

    @Test
    void suggest_WhenDaDataReturnsNull_ShouldReturnNull() {
        // Arrange
        String query = "test query";

        when(geoapifyClient.getAddressSuggestions(eq(query))).thenReturn(null);

        // Act
        List<String> result = addressController.suggest(query);

        // Assert
        assertNull(result);
        verify(geoapifyClient).getAddressSuggestions(query);
    }

    // Tests for setAddress method
    @Test
    void setAddress_WhenValidAddress_ShouldReturnOk() {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doNothing().when(deliveryService).setDeliveryInfo(eq(TEST_USER_ID), eq(TEST_ADDRESS));

        // Act
        ResponseEntity<?> response = addressController.setAddress(TEST_ADDRESS, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).setDeliveryInfo(TEST_USER_ID, TEST_ADDRESS);
    }

    @Test
    void setAddress_WhenAddressNotCorrect_ShouldReturnUnprocessableEntity() {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        String invalidAddress = "invalid address";
        doThrow(new AddressNotCorrectException())
                .when(deliveryService).setDeliveryInfo(eq(TEST_USER_ID), eq(invalidAddress));

        // Act
        ResponseEntity<?> response = addressController.setAddress(invalidAddress, jwt);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());


        assertEquals("Введите существующий адрес с городом, улицей и домом!", response.getBody());
        verify(deliveryService).setDeliveryInfo(TEST_USER_ID, invalidAddress);
    }

    @Test
    void setAddress_WhenEmptyAddress_ShouldHandleGracefully() {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        String emptyAddress = "";
        doNothing().when(deliveryService).setDeliveryInfo(eq(TEST_USER_ID), eq(emptyAddress));

        // Act
        ResponseEntity<?> response = addressController.setAddress(emptyAddress, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).setDeliveryInfo(TEST_USER_ID, emptyAddress);
    }

    @Test
    void setAddress_WhenNullAddress_ShouldHandleGracefully() {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doNothing().when(deliveryService).setDeliveryInfo(eq(TEST_USER_ID), isNull());

        // Act
        ResponseEntity<?> response = addressController.setAddress(null, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).setDeliveryInfo(TEST_USER_ID, null);
    }

    @Test
    void setAddress_WhenServiceThrowsRuntimeException_ShouldPropagate() {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doThrow(new RuntimeException("Service error"))
                .when(deliveryService).setDeliveryInfo(eq(TEST_USER_ID), eq(TEST_ADDRESS));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            addressController.setAddress(TEST_ADDRESS, jwt);
        });
    }

}
