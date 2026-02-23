package com.example.common.mapper;

import com.example.common.dto.category.rest.CategoryResponseDTO;
import com.example.common.enumeration.category.CategoryCode;
import com.example.common.grpc.category.Category;
import com.example.common.grpc.product.CategoryEnum;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ValueMapping;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CategoryMapper {

    public abstract CategoryResponseDTO toCategoryResponseDTO(Category.CategoryResponse category);

    public abstract List<CategoryResponseDTO> toCategoryResponseDTOList(List<Category.CategoryResponse> categoryList);

    @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
    public abstract CategoryCode toCategoryCode(CategoryEnum category);

    public abstract CategoryEnum  toCategoryGrpcCode(CategoryCode  category);


}
