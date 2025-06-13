package com.example.managerapp.record;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

public record Image (Long id, String name, String originalFileName, String contentType, Long size, boolean isPreviewImage, byte[] bytes){
}
