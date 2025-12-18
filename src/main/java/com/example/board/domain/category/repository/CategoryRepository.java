package com.example.board.domain.category.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 이름으로 카테고리 조회 (중복 확인용)
    Optional<Category> findByName(String name);
    
    // 전체 카테고리 목록 (이름순 정렬)
    List<Category> findAllByOrderByNameAsc();
}
