package com.example.managerapp.entity;

public record Image (Long id, String name, String originalFileName, String contentType, Long size, boolean isPreviewImage, byte[] bytes){
}
