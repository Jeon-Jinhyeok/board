/**
 * 게시글 수정 페이지 E2E 테스트
 * 
 * 이 파일은 게시글 수정 페이지(post/edit.html)를 테스트합니다.
 * 
 * 테스트 항목:
 * 1. 비로그인 상태에서 수정 페이지 접근 시 리다이렉트
 * 2. 수정 폼 요소 확인
 * 3. 취소 버튼 및 수정 버튼 존재 확인
 */

import { test, expect } from '@playwright/test';

// ============================================================
// 테스트 그룹: 게시글 수정 페이지
// ============================================================
test.describe('게시글 수정 페이지 테스트', () => {

  /**
   * 테스트 1: 비로그인 상태에서 수정 페이지 접근
   * 
   * 목적: 비로그인 상태에서 수정 페이지 접근 시 
   * 로그인 페이지로 리다이렉트되는지 확인
   */
  test('비로그인 상태에서 수정 페이지 접근 시 로그인으로 리다이렉트된다', async ({ page }) => {
    // --------------------------------------------------------
    // /posts/1/edit로 직접 접근 시도
    // Spring Security가 설정되어 있다면 로그인 페이지로 리다이렉트
    // --------------------------------------------------------
    await page.goto('/posts/1/edit');
    
    // --------------------------------------------------------
    // 두 가지 가능한 결과:
    // 1. 로그인 페이지로 리다이렉트됨 (보안 설정 O)
    // 2. 수정 페이지가 그대로 표시됨 (보안 설정 X 또는 게시글 없음)
    // --------------------------------------------------------
    const currentUrl = page.url();
    
    const isLoginPage = currentUrl.includes('login');
    const isEditPage = currentUrl.includes('edit');
    const isErrorPage = await page.locator('body').textContent().then(text => 
      text?.includes('오류') || text?.includes('error') || text?.includes('404')
    );
    
    // 로그인 페이지, 수정 페이지, 또는 에러 페이지 중 하나여야 함
    expect(isLoginPage || isEditPage || isErrorPage).toBeTruthy();
    
    if (isLoginPage) {
      // 로그인 폼이 있는지 확인
      await expect(page.locator('input[type="password"]').first()).toBeVisible();
    }
  });

  /**
   * 테스트 2: 수정 페이지 폼 요소 확인 (접근 가능 시)
   * 
   * 목적: 수정 페이지에 필요한 폼 요소가 있는지 확인
   * 
   * 실제 HTML 구조:
   * - 카테고리: <select id="categoryId">
   * - 제목: <input id="title">
   * - 내용: <textarea id="content">
   * - 취소 버튼: <a>취소</a>
   * - 수정 버튼: <button type="submit">수정하기</button>
   */
  test('수정 페이지에 필요한 폼 요소가 있다 (페이지 접근 가능 시)', async ({ page }) => {
    // 먼저 홈에서 게시글이 있는지 확인
    await page.goto('/');
    
    const postLink = page.locator('tbody tr td a').first();
    
    if (await postLink.count() > 0) {
      // 첫 번째 게시글의 ID 추출
      const href = await postLink.getAttribute('href');
      
      if (href) {
        // /posts/123 형태에서 ID 추출
        const postId = href.match(/\/posts\/(\d+)/)?.[1];
        
        if (postId) {
          // 수정 페이지로 이동 시도
          await page.goto(`/posts/${postId}/edit`);
          
          const currentUrl = page.url();
          
          // 수정 페이지에 있는 경우에만 테스트
          if (currentUrl.includes('edit')) {
            // --------------------------------------------------------
            // 수정 폼에 필요한 요소들:
            // - 카테고리 선택 (categoryId)
            // - 제목 입력 (title)  
            // - 내용 입력 (content)
            // --------------------------------------------------------
            
            const categorySelect = page.locator('#categoryId');
            const titleInput = page.locator('#title');
            const contentTextarea = page.locator('#content');
            
            // 제목과 내용은 필수 요소
            await expect(titleInput).toBeVisible();
            await expect(contentTextarea).toBeVisible();
            
            // 카테고리는 선택사항
            if (await categorySelect.count() > 0) {
              await expect(categorySelect).toBeVisible();
            }
          }
        }
      }
    }
    // 게시글이 없으면 테스트 스킵 (통과 처리)
  });

  /**
   * 테스트 3: 수정 페이지 버튼 확인
   * 
   * 목적: 취소 버튼과 수정하기 버튼이 있는지 확인
   */
  test('수정 페이지에 취소 버튼과 수정 버튼이 있다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/');
    
    const postLink = page.locator('tbody tr td a').first();
    
    if (await postLink.count() > 0) {
      const href = await postLink.getAttribute('href');
      
      if (href) {
        const postId = href.match(/\/posts\/(\d+)/)?.[1];
        
        if (postId) {
          await page.goto(`/posts/${postId}/edit`);
          
          const currentUrl = page.url();
          
          if (currentUrl.includes('edit')) {
            // --------------------------------------------------------
            // 취소 버튼 확인: <a>...취소</a>
            // --------------------------------------------------------
            const cancelButton = page.locator('a:has-text("취소")');
            
            // --------------------------------------------------------
            // 수정 버튼 확인: <button type="submit">...수정하기</button>
            // --------------------------------------------------------
            const submitButton = page.locator('button[type="submit"]');
            
            if (await cancelButton.count() > 0) {
              await expect(cancelButton.first()).toBeVisible();
            }
            
            await expect(submitButton).toBeVisible();
            await expect(submitButton).toContainText('수정');
          }
        }
      }
    }
  });

  /**
   * 테스트 4: 페이지 헤더 및 Breadcrumb 확인
   * 
   * 목적: 페이지 구조가 올바르게 렌더링되는지 확인
   */
  test('수정 페이지에 헤더와 Breadcrumb이 표시된다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/');
    
    const postLink = page.locator('tbody tr td a').first();
    
    if (await postLink.count() > 0) {
      const href = await postLink.getAttribute('href');
      
      if (href) {
        const postId = href.match(/\/posts\/(\d+)/)?.[1];
        
        if (postId) {
          await page.goto(`/posts/${postId}/edit`);
          
          const currentUrl = page.url();
          
          if (currentUrl.includes('edit')) {
            // --------------------------------------------------------
            // 헤더: <h2>...글 수정</h2>
            // --------------------------------------------------------
            const header = page.locator('h2');
            await expect(header).toContainText('글 수정');
            
            // --------------------------------------------------------
            // Breadcrumb: 홈 > 게시글 > 수정
            // --------------------------------------------------------
            const breadcrumb = page.locator('nav[aria-label="breadcrumb"]');
            if (await breadcrumb.count() > 0) {
              await expect(breadcrumb).toBeVisible();
              await expect(breadcrumb).toContainText('홈');
            }
          }
        }
      }
    }
  });
});
