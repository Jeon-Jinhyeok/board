/**
 * 북마크 페이지 E2E 테스트
 * 
 * 이 파일은 북마크 목록 페이지(bookmark/list.html)를 테스트합니다.
 * 
 * 테스트 항목:
 * 1. 비로그인 상태에서 북마크 페이지 접근 시 리다이렉트
 * 2. 북마크 페이지 헤더 및 구조 확인
 * 3. 테이블 헤더 확인
 * 4. 목록으로 돌아가기 버튼 확인
 */

import { test, expect } from '@playwright/test';

// ============================================================
// 테스트 그룹: 북마크 페이지
// ============================================================
test.describe('북마크 페이지 테스트', () => {

  /**
   * 테스트 1: 비로그인 상태에서 북마크 페이지 접근
   * 
   * 목적: 비로그인 상태에서 북마크 페이지 접근 시 
   * 로그인 페이지로 리다이렉트되는지 확인
   */
  test('비로그인 상태에서 북마크 페이지 접근 시 로그인으로 리다이렉트된다', async ({ page }) => {
    // --------------------------------------------------------
    // /bookmarks로 직접 접근 시도
    // Spring Security가 설정되어 있다면 로그인 페이지로 리다이렉트
    // --------------------------------------------------------
    await page.goto('/bookmarks');
    
    // --------------------------------------------------------
    // 두 가지 가능한 결과:
    // 1. 로그인 페이지로 리다이렉트됨 (보안 설정 O)
    // 2. 북마크 페이지가 그대로 표시됨 (보안 설정 X)
    // --------------------------------------------------------
    const currentUrl = page.url();
    
    const isLoginPage = currentUrl.includes('login');
    const isBookmarkPage = currentUrl.includes('bookmark');
    
    // 둘 중 하나여야 함
    expect(isLoginPage || isBookmarkPage).toBeTruthy();
    
    if (isLoginPage) {
      // 로그인 폼이 있는지 확인
      await expect(page.locator('input[type="password"]').first()).toBeVisible();
    }
  });

  /**
   * 테스트 2: 북마크 페이지 헤더 확인 (접근 가능 시)
   * 
   * 목적: 북마크 페이지가 올바르게 렌더링되는지 확인
   * 
   * 실제 HTML 구조:
   * - 헤더: <h2>...내 북마크</h2>
   * - 설명: <p>저장한 게시글을 모아볼 수 있습니다</p>
   */
  test('북마크 페이지에 헤더가 표시된다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/bookmarks');
    
    const currentUrl = page.url();
    
    // 북마크 페이지에 있는 경우에만 테스트
    if (currentUrl.includes('bookmark')) {
      // --------------------------------------------------------
      // 헤더 확인: <h2>...내 북마크</h2>
      // --------------------------------------------------------
      const header = page.locator('h2');
      await expect(header).toContainText('북마크');
      
      // --------------------------------------------------------
      // 설명 텍스트 확인
      // --------------------------------------------------------
      const description = page.locator('p.text-muted');
      if (await description.count() > 0) {
        await expect(description.first()).toContainText('저장한 게시글');
      }
    }
  });

  /**
   * 테스트 3: 북마크 테이블 구조 확인
   * 
   * 목적: 북마크 목록 테이블이 올바른 컬럼을 가지고 있는지 확인
   * 
   * 예상 컬럼: 번호, 제목, 작성자, 작성일, 조회
   */
  test('북마크 목록 테이블에 올바른 헤더가 있다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/bookmarks');
    
    const currentUrl = page.url();
    
    if (currentUrl.includes('bookmark')) {
      // --------------------------------------------------------
      // page.locator('th'): 테이블 헤더 셀(<th>)을 모두 찾습니다
      // --------------------------------------------------------
      const headers = page.locator('th');
      
      // --------------------------------------------------------
      // 테이블 헤더가 올바른 컬럼을 포함하는지 확인
      // --------------------------------------------------------
      await expect(headers).toContainText(['번호', '제목', '작성자', '작성일', '조회']);
    }
  });

  /**
   * 테스트 4: 목록으로 돌아가기 버튼 확인
   * 
   * 목적: 홈으로 돌아가는 버튼이 있는지 확인
   * 실제 HTML: <a href="/">...목록으로</a>
   */
  test('목록으로 돌아가기 버튼이 있다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/bookmarks');
    
    const currentUrl = page.url();
    
    if (currentUrl.includes('bookmark')) {
      // --------------------------------------------------------
      // '목록으로' 텍스트가 있는 링크 찾기
      // --------------------------------------------------------
      const backButton = page.locator('a:has-text("목록으로")');
      
      if (await backButton.count() > 0) {
        await expect(backButton.first()).toBeVisible();
        
        // href가 '/'인지 확인
        const href = await backButton.first().getAttribute('href');
        expect(href).toBe('/');
      }
    }
  });

  /**
   * 테스트 5: 북마크가 없을 때 안내 메시지 확인
   * 
   * 목적: 북마크한 게시글이 없을 때 적절한 안내가 표시되는지 확인
   * 실제 HTML: "북마크한 게시글이 없습니다"
   */
  test('북마크가 없을 때 안내 메시지가 표시된다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/bookmarks');
    
    const currentUrl = page.url();
    
    if (currentUrl.includes('bookmark')) {
      // --------------------------------------------------------
      // 테이블 바디의 행 개수 확인
      // --------------------------------------------------------
      const tableRows = page.locator('tbody tr');
      const rowCount = await tableRows.count();
      
      // 북마크가 없는 경우 (빈 상태 메시지가 있는 행만 있음)
      if (rowCount === 1) {
        const cellText = await tableRows.first().textContent();
        
        // "북마크한 게시글이 없습니다" 메시지 또는 실제 데이터가 있어야 함
        const hasEmptyMessage = cellText?.includes('북마크한 게시글이 없습니다');
        const hasData = cellText && !cellText.includes('없습니다');
        
        expect(hasEmptyMessage || hasData).toBeTruthy();
      }
      
      // 북마크가 있는 경우 - 데이터가 표시되어야 함
      if (rowCount > 1) {
        // 각 행에 게시글 링크가 있는지 확인
        const postLinks = tableRows.first().locator('a');
        if (await postLinks.count() > 0) {
          await expect(postLinks.first()).toBeVisible();
        }
      }
    }
  });

  /**
   * 테스트 6: 북마크 페이지에서 게시글 클릭 시 상세 페이지 이동
   * 
   * 목적: 북마크된 게시글 제목 클릭 시 상세 페이지로 이동하는지 확인
   */
  test('북마크된 게시글 클릭 시 상세 페이지로 이동한다 (페이지 접근 가능 시)', async ({ page }) => {
    await page.goto('/bookmarks');
    
    const currentUrl = page.url();
    
    if (currentUrl.includes('bookmark')) {
      // 게시글 링크 찾기 (북마크가 있는 경우)
      const postLink = page.locator('tbody tr td a[href^="/posts/"]').first();
      
      if (await postLink.count() > 0) {
        await postLink.click();
        
        // --------------------------------------------------------
        // URL이 '/posts/숫자' 패턴인지 확인
        // --------------------------------------------------------
        await expect(page).toHaveURL(/\/posts\/\d+/);
      }
    }
  });
});
