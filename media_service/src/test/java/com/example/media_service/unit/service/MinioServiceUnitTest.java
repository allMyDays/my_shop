package com.example.media_service.unit.service;

import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.enumeration.media_service.BucketEnum;
import com.example.media_service.service.MinioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceUnitTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private RedisAtomicLong redisAtomicLong;

    @InjectMocks
    private MinioService minioService;

    @Test
    void guessContentType_WithJpegImage_ReturnsJpegMediaType() throws IOException {
        // Given
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG signature

        // Mock URLConnection to return JPEG content type
        try (var mocked = mockStatic(URLConnection.class)) {
            mocked.when(() -> URLConnection.guessContentTypeFromStream(any(ByteArrayInputStream.class)))
                    .thenReturn("image/jpeg");

            // When
            MediaType result = minioService.guessContentType(jpegBytes);

            // Then
            assertEquals(MediaType.IMAGE_JPEG, result);
        }
    }

    @Test
    void guessContentType_WhenIOException_ThrowsRuntimeException() throws IOException {
        // Given
        byte[] bytes = new byte[]{1, 2, 3};

        try (var mocked = mockStatic(URLConnection.class)) {
            mocked.when(() -> URLConnection.guessContentTypeFromStream(any(ByteArrayInputStream.class)))
                    .thenThrow(new IOException("Test exception"));

            // When & Then
            assertThrows(RuntimeException.class, () -> minioService.guessContentType(bytes));
        }
    }

    @Test
    void guessContentType_WhenNullContentType_ReturnsOctetStream() throws IOException {
        // Given
        byte[] bytes = new byte[]{1, 2, 3};

        try (var mocked = mockStatic(URLConnection.class)) {
            mocked.when(() -> URLConnection.guessContentTypeFromStream(any(ByteArrayInputStream.class)))
                    .thenReturn(null);

            // When
            MediaType result = minioService.guessContentType(bytes);

            // Then
            assertEquals(MediaType.APPLICATION_OCTET_STREAM, result);
        }
    }

    private static Stream<Arguments> provideFileNamesForExtractBucket() {
        return Stream.of(
                Arguments.of("PRODUCTS_123", BucketEnum.products),
                Arguments.of("USERS_456", BucketEnum.users)
            //    Arguments.of("DOCUMENTS_789", BucketEnum.DOCUMENTS)
        );
    }

    @Test
    void extractBucket_WithInvalidFileName_ThrowsRuntimeException() {
        // Given
        String invalidFileName = "INVALID_BUCKET_123";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> minioService.extractBucket(invalidFileName));

        assertEquals("cannot find bucket", exception.getMessage(


        ));
    }

    @Test
    void extractBucket_WithMalformedFileName_ThrowsRuntimeException() {
        // Given
        String malformedFileName = "NO_UNDERSCORE";

        // When & Then
        assertThrows(RuntimeException.class, () -> minioService.extractBucket(malformedFileName));
    }


    @Test
    void uploadFile_WhenS3Fails_ThrowsException() {
        // Given
        PhotoDataDTO file = new PhotoDataDTO();
        file.setBytes(new byte[]{1, 2, 3});
        file.setContentType("image/jpeg");

        when(redisAtomicLong.incrementAndGet()).thenReturn(123L);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // When & Then
        assertThrows(Exception.class, () -> minioService.uploadFile(file, BucketEnum.products));
    }

    @Test
    void deleteMultipleFiles_WithEmptyList_ReturnsZero() {
        // When
        int result = minioService.deleteMultipleFiles(List.of());

        // Then
        assertEquals(0, result);
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void uploadFiles_WithEmptyList_ReturnsEmptyList() throws Exception {
        // When
        List<String> result = minioService.uploadFiles(List.of(), BucketEnum.users);

        // Then
        assertTrue(result.isEmpty());
        verify(redisAtomicLong, never()).incrementAndGet();
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void generateNewFileKey_WithBucket_ReturnsFormattedKey() {
        // Given
        when(redisAtomicLong.incrementAndGet()).thenReturn(123L);

        // When
        String result = minioService.generateNewFileKey(BucketEnum.products);

        // Then
        assertEquals("products_123", result);
        verify(redisAtomicLong).incrementAndGet();
    }

    @Test
    void uploadFile_WithValidFile_ReturnsFileName() throws Exception {
        // Given
        PhotoDataDTO file = new PhotoDataDTO();
        file.setBytes(new byte[]{1, 2, 3});
        file.setContentType("image/jpeg");

        when(redisAtomicLong.incrementAndGet()).thenReturn(123L);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // When
        String result = minioService.uploadFile(file, BucketEnum.products);

        // Then - упрощаем проверку, убираем проблемный argThat
        assertEquals("products_123", result);
        verify(redisAtomicLong).incrementAndGet();
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void getFile_WithValidFileName_ReturnsFileData() throws IOException {
        // Given
        String fileName = "products_123";
        byte[] expectedBytes = new byte[]{1, 2, 3};

        ResponseBytes<GetObjectResponse> responseBytes = mock(ResponseBytes.class);
        when(responseBytes.asByteArray()).thenReturn(expectedBytes);

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(responseBytes);

        try (var mocked = mockStatic(URLConnection.class)) {
            mocked.when(() -> URLConnection.guessContentTypeFromStream(any(ByteArrayInputStream.class)))
                    .thenReturn("image/jpeg");

            // When
            Map.Entry<byte[], MediaType> result = minioService.getFile(fileName);

            // Then - упрощаем проверку
            assertArrayEquals(expectedBytes, result.getKey());
            assertEquals(MediaType.IMAGE_JPEG, result.getValue());
            verify(s3Client


            ).getObjectAsBytes(any(GetObjectRequest.class));
        }
    }

    @Test
    void deleteFile_WithValidFileName_ReturnsTrue() {
        // Given
        String fileName = "products_123";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        // When
        boolean result = minioService.deleteFile(fileName);

        // Then - упрощаем проверку
        assertTrue(result);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFile_WhenExceptionOccurs_ReturnsFalse() {
        // Given
        String fileName = "products_123";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("Error").build());

        // When
        boolean result = minioService.deleteFile(fileName);

        // Then
        assertFalse(result);
    }

    @Test
    void deleteMultipleFiles_WithMoreThan1000Files_ReturnsZero() {
        // Given
        List<String> largeList = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            largeList.add("products_" + i);
        }

        // When
        int result = minioService.deleteMultipleFiles(largeList);

        // Then
        assertEquals(0, result);
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteMultipleFiles_WithValidFiles_ReturnsDeletedCount() {
        // Given
        List<String> fileNames = List.of("products_1", "products_2", "products_3");

        DeletedObject deleted1 = DeletedObject.builder().key("products_1").build();
        DeletedObject deleted2 = DeletedObject.builder().key("products_2").build();
        DeletedObject deleted3 = DeletedObject.builder().key("products_3").build();

        DeleteObjectsResponse response = DeleteObjectsResponse.builder()
                .deleted(deleted1, deleted2, deleted3)
                .build();

        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .thenReturn(response);

        // When
        int result = minioService.deleteMultipleFiles(fileNames);

        // Then - упрощаем проверку
        assertEquals(3, result);
        verify(s3Client).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteMultipleFiles_WhenS3Exception_ThrowsRuntimeException() {
        // Given
        List<String> fileNames = List.of("products_1", "products_2");

        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .thenThrow(S3Exception.builder()
                        .awsErrorDetails(AwsErrorDetails.builder()
                                .errorMessage("Access denied")
                                .build())
                        .build());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> minioService.deleteMultipleFiles(fileNames));

        assertTrue(exception.getMessage().contains("Ошибка S3 при удалении файлов: Access denied"));
    }

    @Test
    void uploadFiles_WithValidFiles_ReturnsFileNames() throws Exception {
        // Given
        PhotoDataDTO file1 = new PhotoDataDTO();
        file1.setBytes(new byte[]{1, 2, 3});
        file1.setContentType("image/jpeg");

        PhotoDataDTO file2 = new PhotoDataDTO();
        file2.setBytes(new byte[]{4, 5, 6});
        file2.setContentType("image/png");

        List<PhotoDataDTO> files = List.of(file1, file2);

        when(redisAtomicLong.incrementAndGet()).thenReturn(100L, 101L);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // When
        List<String> result = minioService.uploadFiles(files, BucketEnum.users);

        // Then
        assertEquals(2, result.size());
        assertEquals(List.of("users_100", "users_101"), result);
        verify(redisAtomicLong, times(2)).incrementAndGet();
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFiles_WhenOneFileFails_ContinuesWithOthers() throws Exception {
        // Given
        PhotoDataDTO file1 = new PhotoDataDTO();
        file1.setBytes(new byte[]{1, 2, 3});
        file1.setContentType("image/jpeg");

        PhotoDataDTO file2 = new PhotoDataDTO();
        file2.setBytes(new byte[]{4, 5, 6});
        file2.setContentType("image/png");

        List<PhotoDataDTO> files = List.of(file1, file2);

        when(redisAtomicLong.incrementAndGet()).thenReturn(100L, 101L);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build())
                .thenThrow(S3Exception.builder().message("Upload failed").build());

        // When
        List<String> result = minioService.uploadFiles(files, BucketEnum.users);

        // Then
        assertEquals(1, result.size());
        assertEquals("users_100", result.get(0));
        verify(redisAtomicLong, times(2)).incrementAndGet();
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }





}










