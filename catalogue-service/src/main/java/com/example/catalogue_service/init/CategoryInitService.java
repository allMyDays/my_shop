package com.example.catalogue_service.init;

import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.repository.CategoryRepository;
import com.example.common.enumeration.category.CategoryCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryInitService {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initCategories(){

        for(CategoryCode categoryCode : CategoryCode.values()) {
            if(!categoryRepository.existsByCode(categoryCode)){
                categoryRepository.save(new Category(categoryCode.getName(), categoryCode));
            }

        }
    }



}
