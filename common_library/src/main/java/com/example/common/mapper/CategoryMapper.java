package com.example.common.mapper;

import com.example.common.dto.category.rest.CategoryResponseDTO;
import com.example.common.grpc.category.Category;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CategoryMapper {

    public abstract CategoryResponseDTO toCategoryResponseDTO(Category.CategoryResponse category);

    public abstract List<CategoryResponseDTO> toCategoryResponseDTOList(List<Category.CategoryResponse> categoryList);




}
