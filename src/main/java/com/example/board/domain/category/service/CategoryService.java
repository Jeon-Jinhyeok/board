package com.example.board.domain.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.domain.category.dto.CategoryResponse;
import com.example.board.domain.category.entity.Category;
import com.example.board.domain.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;

    // 전체 카테고리 조회
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 카테고리 ID로 조회
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }

    // 새 카테고리 생성
    @Transactional
    public Category createCategory(String name) {
        // 중복 검사
        if (categoryRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + name);
        }
        
        Category category = Category.builder()
                .name(name)
                .build();
        
        return categoryRepository.save(category);
    }

    // 글 작성 시 사용: 기존 카테고리 있으면 반환, 없으면 생성
    @Transactional
    public Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name(name)
                            .build();
                    return categoryRepository.save(newCategory);
                });
    }
}
