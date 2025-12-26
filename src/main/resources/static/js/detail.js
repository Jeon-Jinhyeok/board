/**
 * 게시글 상세 페이지 JavaScript
 */

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", function() {
    initCommentCharCount();
});

/**
 * 댓글 글자수 카운트 초기화
 */
function initCommentCharCount() {
    const commentContent = document.getElementById("commentContent");
    const charCount = document.getElementById("charCount");
    
    if (commentContent && charCount) {
        commentContent.addEventListener("input", function() {
            charCount.textContent = this.value.length;
        });
    }
}

/**
 * 댓글 작성
 */
async function submitComment() {
    const content = document.getElementById("commentContent").value.trim();
    if (!content) {
        alert("댓글 내용을 입력해주세요.");
        return;
    }

    try {
        const response = await postJson(`/api/posts/${postId}/comments`, { content });
        
        if (response.ok) {
            location.reload();
        } else {
            await showApiError(response, "댓글 작성에 실패했습니다.");
        }
    } catch (error) {
        handleApiError(error, "댓글 작성 중 오류가 발생했습니다.");
    }
}

/**
 * 대댓글 폼 표시
 * @param {number} commentId - 부모 댓글 ID
 */
function showReplyForm(commentId) {
    document.getElementById("replyForm-" + commentId).classList.remove("d-none");
}

/**
 * 대댓글 폼 숨기기
 * @param {number} commentId - 부모 댓글 ID
 */
function hideReplyForm(commentId) {
    document.getElementById("replyForm-" + commentId).classList.add("d-none");
    document.getElementById("replyContent-" + commentId).value = "";
}

/**
 * 대댓글 작성
 * @param {number} parentId - 부모 댓글 ID
 */
async function submitReply(parentId) {
    const content = document.getElementById("replyContent-" + parentId).value.trim();
    if (!content) {
        alert("답글 내용을 입력해주세요.");
        return;
    }

    try {
        const response = await postJson(`/api/posts/${postId}/comments`, {
            content: content,
            parentId: parentId
        });
        
        if (response.ok) {
            location.reload();
        } else {
            await showApiError(response, "답글 작성에 실패했습니다.");
        }
    } catch (error) {
        handleApiError(error, "답글 작성 중 오류가 발생했습니다.");
    }
}

/**
 * 댓글 수정
 * @param {number} commentId - 댓글 ID
 */
async function editComment(commentId) {
    const commentEl = document.getElementById("comment-" + commentId);
    const contentEl = commentEl.querySelector("p");
    const currentContent = contentEl.textContent;

    const newContent = prompt("댓글을 수정하세요:", currentContent);
    if (newContent === null || newContent.trim() === "") return;
    if (newContent === currentContent) return;

    try {
        const response = await putJson(
            `/api/posts/${postId}/comments/${commentId}`,
            { content: newContent.trim() }
        );
        
        if (response.ok) {
            location.reload();
        } else {
            await showApiError(response, "댓글 수정에 실패했습니다.");
        }
    } catch (error) {
        handleApiError(error, "댓글 수정 중 오류가 발생했습니다.");
    }
}

/**
 * 댓글 삭제
 * @param {number} commentId - 댓글 ID
 */
async function deleteComment(commentId) {
    await confirmAndDelete(
        `/api/posts/${postId}/comments/${commentId}`,
        "정말 삭제하시겠습니까?"
    );
}

/**
 * 좋아요 토글
 */
async function toggleLike() {
    await toggleReaction("like");
}

/**
 * 싫어요 토글
 */
async function toggleDislike() {
    await toggleReaction("dislike");
}

/**
 * 북마크 토글
 */
async function toggleBookmark() {
    try {
        const response = await postJson(`/api/bookmarks/${postId}`, {});
        const data = await response.json();
        
        const btn = document.getElementById("bookmarkBtn");
        const icon = btn.querySelector("i");

        if (data.bookmarked) {
            btn.classList.remove("btn-outline-warning");
            btn.classList.add("btn-warning");
            icon.classList.remove("bi-bookmark");
            icon.classList.add("bi-bookmark-fill");
        } else {
            btn.classList.remove("btn-warning");
            btn.classList.add("btn-outline-warning");
            icon.classList.remove("bi-bookmark-fill");
            icon.classList.add("bi-bookmark");
        }
    } catch (error) {
        handleApiError(error, "북마크 처리 중 오류가 발생했습니다.");
    }
}

/**
 * 좋아요/싫어요 반응 토글
 * @param {string} type - 'like' 또는 'dislike'
 */
async function toggleReaction(type) {
    try {
        const response = await postJson(`/api/posts/${postId}/${type}`, {});
        const data = await response.json();

        // 카운트 업데이트
        document.getElementById("likeCount").textContent = data.likeCount;
        document.getElementById("dislikeCount").textContent = data.dislikeCount;

        // 버튼 스타일 업데이트
        updateButtonStyles(type, data.result);
    } catch (error) {
        handleApiError(error, "반응 처리 중 오류가 발생했습니다.");
    }
}

/**
 * 좋아요/싫어요 버튼 스타일 업데이트
 * @param {string} type - 'like' 또는 'dislike'
 * @param {string} result - 'created', 'changed', 'cancelled'
 */
function updateButtonStyles(type, result) {
    const likeBtn = document.getElementById("likeBtn");
    const dislikeBtn = document.getElementById("dislikeBtn");

    // 모든 버튼 초기화
    likeBtn.classList.remove("btn-primary");
    likeBtn.classList.add("btn-outline-primary");
    dislikeBtn.classList.remove("btn-secondary");
    dislikeBtn.classList.add("btn-outline-secondary");

    // 활성화된 버튼 스타일 적용
    if (result === "created" || result === "changed") {
        if (type === "like") {
            likeBtn.classList.remove("btn-outline-primary");
            likeBtn.classList.add("btn-primary");
        } else {
            dislikeBtn.classList.remove("btn-outline-secondary");
            dislikeBtn.classList.add("btn-secondary");
        }
    }
}
