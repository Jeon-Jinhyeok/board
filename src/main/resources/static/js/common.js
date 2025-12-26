/**
 * 공통 유틸리티 함수 모음
 */

/**
 * 인증이 필요한 API 호출을 위한 fetch
 * 401 에러 시 자동으로 로그인 페이지로 리다이렉트
 * @param {string} url - API URL
 * @param {object} options - fetch options
 * @returns {Promise<Response>}
 */
async function fetchWithAuth(url, options = {}) {
    const response = await fetch(url, options);
    
    if (response.status === 401) {
        alert("로그인이 필요합니다.");
        window.location.href = "/users/login";
        throw new Error("Unauthorized");
    }
    
    return response;
}

/**
 * API 에러 처리 헬퍼
 * @param {Error} error - 에러 객체
 * @param {string} defaultMessage - 기본 에러 메시지
 */
function handleApiError(error, defaultMessage = "오류가 발생했습니다.") {
    console.error("Error:", error);
    if (error.message !== "Unauthorized") {
        alert(error.message || defaultMessage);
    }
}

/**
 * API 응답에서 에러 메시지 추출 및 표시
 * @param {Response} response - fetch Response 객체
 * @param {string} defaultMessage - 기본 에러 메시지
 */
async function showApiError(response, defaultMessage = "요청 처리에 실패했습니다.") {
    try {
        const data = await response.json();
        alert(data.error || data.message || defaultMessage);
    } catch {
        alert(defaultMessage);
    }
}

/**
 * 확인 다이얼로그 후 삭제 요청
 * @param {string} url - 삭제 API URL
 * @param {string} confirmMessage - 확인 메시지
 * @param {Function} onSuccess - 성공 시 콜백
 */
async function confirmAndDelete(url, confirmMessage = "정말 삭제하시겠습니까?", onSuccess = () => location.reload()) {
    if (!confirm(confirmMessage)) return;
    
    try {
        const response = await fetchWithAuth(url, { method: "DELETE" });
        
        if (response.ok) {
            onSuccess();
        } else {
            await showApiError(response, "삭제에 실패했습니다.");
        }
    } catch (error) {
        handleApiError(error, "삭제 중 오류가 발생했습니다.");
    }
}

/**
 * JSON POST 요청 헬퍼
 * @param {string} url - API URL
 * @param {object} data - 전송할 데이터
 * @returns {Promise<Response>}
 */
async function postJson(url, data) {
    return fetchWithAuth(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
}

/**
 * JSON PUT 요청 헬퍼
 * 
 * @param {string} url - API URL
 * @param {object} data - 전송할 데이터
 * @returns {Promise<Response>}
 */
async function putJson(url, data) {
    return fetchWithAuth(url, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
}
