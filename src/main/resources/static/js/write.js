/**
 * 게시글 작성 페이지 JavaScript
 */

/**
 * 새 카테고리 입력 필드 토글
 * @param {HTMLSelectElement} select - 카테고리 select 요소
 */
function toggleNewCategory(select) {
    const newCategoryDiv = document.getElementById('newCategoryDiv');
    const newCategoryInput = document.getElementById('newCategoryName');
    
    if (select.value === 'new') {
        newCategoryDiv.style.display = 'block';
        newCategoryInput.focus();
        // categoryId를 비워서 서버에서 newCategoryName을 사용하도록 함
        select.value = '';
    } else {
        newCategoryDiv.style.display = 'none';
        newCategoryInput.value = '';
    }
}
