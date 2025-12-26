import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 설정 파일
 * 
 * 이 파일은 Playwright 테스트 실행 방식을 정의합니다.
 * - 테스트 대상 URL (baseURL)
 * - 사용할 브라우저
 * - 타임아웃 설정
 * - 스크린샷/비디오 저장 방식
 */
export default defineConfig({
  // 테스트 파일 위치
  testDir: './tests',
  
  // 테스트 실행 설정
  fullyParallel: true,           // 테스트를 병렬로 실행
  forbidOnly: !!process.env.CI,  // CI 환경에서 .only 사용 금지
  retries: process.env.CI ? 2 : 0, // CI에서는 실패 시 2번 재시도
  workers: process.env.CI ? 1 : undefined, // CI에서는 단일 워커 사용
  
  // 리포터 설정 (테스트 결과 출력 방식)
  reporter: 'html',
  
  // 모든 테스트에 적용되는 공통 설정
  use: {
    // Spring Boot 서버 주소
    baseURL: 'http://localhost:8080',
    
    // 실패 시 스크린샷 촬영
    screenshot: 'only-on-failure',
    
    // 실패 시 비디오 녹화
    video: 'retain-on-failure',
    
    // 각 액션 추적 (디버깅용)
    trace: 'on-first-retry',
  },

  // 테스트할 브라우저 목록
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // 필요 시 다른 브라우저 추가 가능
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
  ],

  // 테스트 시 자동으로 Spring Boot 시작
  webServer: {
    command: 'cd .. && ./gradlew bootRun',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
