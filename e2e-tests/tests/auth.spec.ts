/**
 * 인증 페이지 E2E 테스트
 * 
 * 이 파일은 로그인/회원가입 페이지를 테스트합니다.
 * 
 * 테스트 항목:
 * 1. 로그인 페이지 접근 및 폼 요소 확인
 * 2. 회원가입 페이지 접근 및 폼 요소 확인
 * 3. 페이지 간 네비게이션 확인
 */

import { test, expect } from '@playwright/test';

// ============================================================
// 테스트 그룹: 로그인 페이지
// ============================================================
test.describe('로그인 페이지 테스트', () => {

  /**
   * 테스트 1: 로그인 페이지 접근
   * 
   * 목적: 로그인 페이지가 정상적으로 렌더링되는지 확인
   */
  test('로그인 페이지에 접속할 수 있다', async ({ page }) => {
    // --------------------------------------------------------
    // '/user/login' 경로로 이동
    // Spring Security의 기본 로그인 페이지 또는 커스텀 페이지
    // --------------------------------------------------------
    await page.goto('/user/login');
    
    // --------------------------------------------------------
    // 페이지 URL이 '/login'을 포함하는지 확인
    // toHaveURL(): 현재 URL을 검증하는 매처(matcher)
    // 정규식을 사용해 부분 일치 확인
    // --------------------------------------------------------
    await expect(page).toHaveURL(/login/);
  });

  /**
   * 테스트 2: 로그인 폼 요소 확인
   * 
   * 목적: 로그인에 필요한 입력 필드가 존재하는지 확인
   * 
   * 실제 HTML 구조:
   * - 아이디: <input type="text" id="loginId" ...>
   * - 비밀번호: <input type="password" id="password" ...>
   */
  test('로그인 폼에 아이디와 비밀번호 입력 필드가 있다', async ({ page }) => {
    await page.goto('/user/login');
    
    // --------------------------------------------------------
    // page.locator('#loginId'): id가 loginId인 요소를 찾습니다
    // 실제 HTML: <input type="text" id="loginId" ...>
    // --------------------------------------------------------
    const loginIdInput = page.locator('#loginId');
    
    // --------------------------------------------------------
    // page.locator('#password'): id가 password인 요소를 찾습니다
    // 실제 HTML: <input type="password" id="password" ...>
    // --------------------------------------------------------
    const passwordInput = page.locator('#password');
    
    // --------------------------------------------------------
    // toBeVisible(): 요소가 DOM에 있고, 화면에 보이는지 확인
    // Playwright는 자동으로 요소가 나타날 때까지 대기 (기본 30초)
    // --------------------------------------------------------
    await expect(loginIdInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
  });

  /**
   * 테스트 3: 로그인 버튼 확인
   * 
   * 목적: 로그인 제출 버튼이 존재하는지 확인
   * 실제 HTML: <button type="submit">...로그인</button>
   */
  test('로그인 버튼이 존재한다', async ({ page }) => {
    await page.goto('/user/login');
    
    // --------------------------------------------------------
    // page.locator('button[type="submit"]'): submit 버튼을 찾습니다
    // --------------------------------------------------------
    const loginButton = page.locator('button[type="submit"]');
    
    await expect(loginButton).toBeVisible();
    // 버튼 텍스트에 '로그인'이 포함되어 있는지 확인
    await expect(loginButton).toContainText('로그인');
  });

  /**
   * 테스트 4: 회원가입 링크 확인
   * 
   * 목적: 로그인 페이지에서 회원가입 페이지로 이동할 수 있는지 확인
   * 실제 HTML: <a href="/users/signup">회원가입 ...</a>
   */
  test('회원가입 페이지로 이동하는 링크가 있다', async ({ page }) => {
    await page.goto('/user/login');
    
    // --------------------------------------------------------
    // page.locator('a[href*="signup"]'): href에 signup이 포함된 링크
    // --------------------------------------------------------
    const signupLink = page.locator('a[href*="signup"]');
    
    await expect(signupLink).toBeVisible();
  });
});

// ============================================================
// 테스트 그룹: 회원가입 페이지
// ============================================================
test.describe('회원가입 페이지 테스트', () => {

  /**
   * 테스트 1: 회원가입 페이지 접근
   * 
   * 참고: 로그인 페이지의 링크는 /users/signup으로 되어있음
   */
  test('회원가입 페이지에 접속할 수 있다', async ({ page }) => {
    // 로그인 페이지의 링크 경로인 /users/signup으로 시도
    await page.goto('/users/signup');
    
    // URL에 signup이 포함되어 있는지 확인
    await expect(page).toHaveURL(/signup/);
  });

  /**
   * 테스트 2: 회원가입 폼 요소 확인
   * 
   * 목적: 회원가입에 필요한 모든 입력 필드가 있는지 확인
   * 
   * 실제 HTML 구조:
   * - 아이디: <input type="text" id="loginId">
   * - 비밀번호: <input type="password" id="password">
   * - 닉네임: <input type="text" id="username">
   */
  test('회원가입 폼에 필요한 입력 필드가 있다', async ({ page }) => {
    await page.goto('/users/signup');
    
    // --------------------------------------------------------
    // 회원가입에 필요한 필드들:
    // - 아이디 (loginId)
    // - 비밀번호 (password)
    // - 닉네임 (username)
    // --------------------------------------------------------
    
    const loginIdInput = page.locator('#loginId');
    const passwordInput = page.locator('#password');
    const usernameInput = page.locator('#username');
    
    await expect(loginIdInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
    await expect(usernameInput).toBeVisible();
  });

  /**
   * 테스트 3: 회원가입 버튼 확인
   * 
   * 실제 HTML: <button type="submit">...가입하기</button>
   */
  test('회원가입 버튼이 존재한다', async ({ page }) => {
    await page.goto('/users/signup');
    
    // --------------------------------------------------------
    // submit 버튼을 찾고 '가입하기' 텍스트가 포함되어 있는지 확인
    // --------------------------------------------------------
    const signupButton = page.locator('button[type="submit"]');
    
    await expect(signupButton).toBeVisible();
    await expect(signupButton).toContainText('가입하기');
  });
});
