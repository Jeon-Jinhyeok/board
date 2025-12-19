package com.example.board.domain.category.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.board.domain.category.dto.CategoryResponse;
import com.example.board.domain.category.entity.Category;
import com.example.board.domain.category.repository.CategoryRepository;

/**
 * CategoryService 단위 테스트
 * 
 * Narrative: 카테고리 서비스는 게시글 카테고리의 조회, 생성 기능을 제공하며,
 *            중복 카테고리를 방지하고 필요시 새 카테고리를 생성한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 단위 테스트")
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .name("자유게시판")
                .build();
        ReflectionTestUtils.setField(testCategory, "id", 1L);
    }

    @Nested
    @DisplayName("전체 카테고리 조회 기능")
    class GetAllCategoriesTest {

        @Test
        @DisplayName("성공: 모든 카테고리를 이름순으로 조회한다")
        void getAllCategories_ReturnsAllCategoriesInOrder() {
            // Given: 여러 카테고리가 존재할 때
            Category category2 = Category.builder().name("공지사항").build();
            ReflectionTestUtils.setField(category2, "id", 2L);

            Category category3 = Category.builder().name("질문게시판").build();
            ReflectionTestUtils.setField(category3, "id", 3L);

            given(categoryRepository.findAllByOrderByNameAsc())
                    .willReturn(List.of(category2, testCategory, category3));

            // When: 전체 카테고리를 조회하면
            List<CategoryResponse> categories = categoryService.getAllCategories();

            // Then: 이름순으로 정렬된 카테고리 목록이 반환된다
            assertThat(categories).hasSize(3);
            assertThat(categories.get(0).getName()).isEqualTo("공지사항");
            assertThat(categories.get(1).getName()).isEqualTo("자유게시판");
            assertThat(categories.get(2).getName()).isEqualTo("질문게시판");
        }

        @Test
        @DisplayName("성공: 카테고리가 없으면 빈 목록을 반환한다")
        void getAllCategories_WhenEmpty_ReturnsEmptyList() {
            // Given: 카테고리가 없을 때
            given(categoryRepository.findAllByOrderByNameAsc()).willReturn(List.of());

            // When: 전체 카테고리를 조회하면
            List<CategoryResponse> categories = categoryService.getAllCategories();

            // Then: 빈 목록이 반환된다
            assertThat(categories).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리 ID로 조회 기능")
    class FindByIdTest {

        @Test
        @DisplayName("성공: 존재하는 카테고리 ID로 조회하면 카테고리를 반환한다")
        void findById_WithExistingId_ReturnsCategory() {
            // Given: 존재하는 카테고리 ID가 있을 때
            Long categoryId = 1L;
            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(testCategory));

            // When: 카테고리를 조회하면
            Category category = categoryService.findById(categoryId);

            // Then: 카테고리 정보가 반환된다
            assertThat(category).isNotNull();
            assertThat(category.getName()).isEqualTo("자유게시판");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리 ID로 조회하면 예외가 발생한다")
        void findById_WithNonExistentId_ThrowsException() {
            // Given: 존재하지 않는 카테고리 ID로 조회할 때
            Long categoryId = 999L;
            given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> categoryService.findById(categoryId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 카테고리입니다.");
        }
    }

    @Nested
    @DisplayName("새 카테고리 생성 기능")
    class CreateCategoryTest {

        @Test
        @DisplayName("성공: 새로운 카테고리 이름으로 생성하면 카테고리가 생성된다")
        void createCategory_WithNewName_CreatesCategory() {
            // Given: 존재하지 않는 카테고리 이름으로 생성을 요청할 때
            String newCategoryName = "새카테고리";
            Category newCategory = Category.builder().name(newCategoryName).build();
            ReflectionTestUtils.setField(newCategory, "id", 2L);

            given(categoryRepository.findByName(newCategoryName)).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willReturn(newCategory);

            // When: 카테고리를 생성하면
            Category category = categoryService.createCategory(newCategoryName);

            // Then: 카테고리가 생성되고 반환된다
            assertThat(category).isNotNull();
            assertThat(category.getName()).isEqualTo(newCategoryName);
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("실패: 이미 존재하는 카테고리 이름으로 생성하면 예외가 발생한다")
        void createCategory_WithExistingName_ThrowsException() {
            // Given: 이미 존재하는 카테고리 이름으로 생성을 요청할 때
            String existingCategoryName = "자유게시판";
            given(categoryRepository.findByName(existingCategoryName)).willReturn(Optional.of(testCategory));

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> categoryService.createCategory(existingCategoryName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 카테고리입니다: " + existingCategoryName);
        }
    }

    @Nested
    @DisplayName("카테고리 조회 또는 생성 기능")
    class GetOrCreateCategoryTest {

        @Test
        @DisplayName("성공: 기존 카테고리가 있으면 해당 카테고리를 반환한다")
        void getOrCreateCategory_WithExistingCategory_ReturnsExisting() {
            // Given: 이미 존재하는 카테고리 이름으로 요청할 때
            String categoryName = "자유게시판";
            given(categoryRepository.findByName(categoryName)).willReturn(Optional.of(testCategory));

            // When: 카테고리를 조회/생성하면
            Category category = categoryService.getOrCreateCategory(categoryName);

            // Then: 기존 카테고리가 반환된다
            assertThat(category).isEqualTo(testCategory);
            then(categoryRepository).should(never()).save(any(Category.class));
        }

        @Test
        @DisplayName("성공: 기존 카테고리가 없으면 새로 생성하여 반환한다")
        void getOrCreateCategory_WithNewCategory_CreatesAndReturns() {
            // Given: 존재하지 않는 카테고리 이름으로 요청할 때
            String newCategoryName = "새카테고리";
            Category newCategory = Category.builder().name(newCategoryName).build();
            ReflectionTestUtils.setField(newCategory, "id", 2L);

            given(categoryRepository.findByName(newCategoryName)).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willReturn(newCategory);

            // When: 카테고리를 조회/생성하면
            Category category = categoryService.getOrCreateCategory(newCategoryName);

            // Then: 새 카테고리가 생성되고 반환된다
            assertThat(category).isNotNull();
            assertThat(category.getName()).isEqualTo(newCategoryName);
            then(categoryRepository).should().save(any(Category.class));
        }
    }
}

