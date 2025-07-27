package com.example.managerapp.mapper;

import com.example.catalogue.grpc.Category;
import com.example.managerapp.dto.category.CategoryResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CategoryMapper {

    public abstract CategoryResponseDTO toCategoryResponseDTO(Category.CategoryResponse category);

    public abstract List<CategoryResponseDTO> toCategoryResponseDTOList(List<Category.CategoryResponse> categoryList);




}
