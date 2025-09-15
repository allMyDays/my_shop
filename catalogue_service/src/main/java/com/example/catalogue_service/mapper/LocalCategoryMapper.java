package com.example.catalogue_service.mapper;
import com.example.common.grpc.category.Category;
import com.example.common.dto.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocalCategoryMapper {


    List<Category.CategoryResponse> toCategoryResponseList(List<com.example.catalogue_service.entity.Category> categories);

    @Named("toCategoryResponse")
    default Category.CategoryResponse toCategoryResponse(com.example.catalogue_service.entity.Category category) {
        return Category.CategoryResponse
                .newBuilder()
                .setId(category.getId())
                .setName(category.getName())
                .build();

    }

    CategoryResponseDTO toCategoryResponseDTO(com.example.catalogue_service.entity.Category category);

    List<CategoryResponseDTO> toCategoryResponseDTOs(List<com.example.catalogue_service.entity.Category> categories);





}
