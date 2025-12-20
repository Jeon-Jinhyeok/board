package com.example.board.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.board.domain.user.dto.LoginRequest;
import com.example.board.domain.user.dto.SignupRequest;
import com.example.board.domain.user.entity.Role;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

/**
 * UserService 단위 테스트
 * 
 * Narrative: 사용자 서비스는 회원가입, 로그인, 사용자 조회 기능을 제공한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입 기능")
    class SignupTest {

        @Test
        @DisplayName("성공: 유효한 회원정보로 회원가입하면 사용자 ID를 반환한다")
        void signup_WithValidRequest_ReturnsUserId() {
            // Given: 유효한 회원가입 요청이 있고, 중복된 아이디나 이름이 없을 때
            SignupRequest request = new SignupRequest();
            request.setLoginId("testuser");
            request.setPassword("password123");
            request.setUsername("테스트유저");

            User savedUser = User.builder()
                    .loginId("testuser")
                    .password("encodedPassword")
                    .username("테스트유저")
                    .role(Role.USER)
                    .build();

            given(userRepository.existsByLoginId("testuser")).willReturn(false);
            given(userRepository.existsByUsername("테스트유저")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // When: 회원가입을 시도하면
            Long userId = userService.signup(request);

            // Then: 사용자가 성공적으로 생성되고 ID가 반환된다
            assertThat(userId).isEqualTo(savedUser.getId());
            then(userRepository).should().save(any(User.class));
            then(passwordEncoder).should().encode("password123");
        }

        @Test
        @DisplayName("실패: 이미 존재하는 아이디로 회원가입하면 예외가 발생한다")
        void signup_WithDuplicateLoginId_ThrowsException() {
            // Given: 이미 존재하는 아이디로 회원가입을 요청할 때
            SignupRequest request = new SignupRequest();
            request.setLoginId("existinguser");
            request.setPassword("password123");
            request.setUsername("새유저");

            given(userRepository.existsByLoginId("existinguser")).willReturn(true);

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 아이디입니다.");
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이름으로 회원가입하면 예외가 발생한다")
        void signup_WithDuplicateUsername_ThrowsException() {
            // Given: 이미 존재하는 이름으로 회원가입을 요청할 때
            SignupRequest request = new SignupRequest();
            request.setLoginId("newuser");
            request.setPassword("password123");
            request.setUsername("존재하는유저");

            given(userRepository.existsByLoginId("newuser")).willReturn(false);
            given(userRepository.existsByUsername("존재하는유저")).willReturn(true);

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 이름입니다.");
        }
    }

    @Nested
    @DisplayName("로그인 기능")
    class LoginTest {

        @Test
        @DisplayName("성공: 올바른 아이디와 비밀번호로 로그인하면 User 객체를 반환한다")
        void login_WithValidCredentials_ReturnsUser() {
            // Given: 유효한 로그인 정보가 있을 때
            LoginRequest request = new LoginRequest();
            request.setLoginId("testuser");
            request.setPassword("password123");

            User user = User.builder()
                    .loginId("testuser")
                    .password("encodedPassword")
                    .username("테스트유저")
                    .role(Role.USER)
                    .build();

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // When: 로그인을 시도하면
            User loggedInUser = userService.login(request);

            // Then: User 객체가 반환된다
            assertThat(loggedInUser).isNotNull();
            assertThat(loggedInUser.getLoginId()).isEqualTo("testuser");
            assertThat(loggedInUser.getUsername()).isEqualTo("테스트유저");
            assertThat(loggedInUser.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 아이디로 로그인하면 예외가 발생한다")
        void login_WithNonExistentLoginId_ThrowsException() {
            // Given: 존재하지 않는 아이디로 로그인을 시도할 때
            LoginRequest request = new LoginRequest();
            request.setLoginId("nonexistent");
            request.setPassword("password123");

            given(userRepository.findByLoginId("nonexistent")).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 아이디입니다.");
        }

        @Test
        @DisplayName("실패: 틀린 비밀번호로 로그인하면 예외가 발생한다")
        void login_WithWrongPassword_ThrowsException() {
            // Given: 틀린 비밀번호로 로그인을 시도할 때
            LoginRequest request = new LoginRequest();
            request.setLoginId("testuser");
            request.setPassword("wrongpassword");

            User user = User.builder()
                    .loginId("testuser")
                    .password("encodedPassword")
                    .username("테스트유저")
                    .role(Role.USER)
                    .build();

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("사용자 조회 기능")
    class FindByIdTest {

        @Test
        @DisplayName("성공: 존재하는 사용자 ID로 조회하면 사용자 정보를 반환한다")
        void findById_WithExistingId_ReturnsUser() {
            // Given: 존재하는 사용자 ID가 있을 때
            Long userId = 1L;
            User user = User.builder()
                    .loginId("testuser")
                    .password("encodedPassword")
                    .username("테스트유저")
                    .role(Role.USER)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When: 사용자를 조회하면
            User foundUser = userService.findById(userId);

            // Then: 사용자 정보가 반환된다
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getLoginId()).isEqualTo("testuser");
            assertThat(foundUser.getUsername()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID로 조회하면 null을 반환한다")
        void findById_WithNonExistentId_ReturnsNull() {
            // Given: 존재하지 않는 사용자 ID가 있을 때
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When: 사용자를 조회하면
            User foundUser = userService.findById(userId);

            // Then: null이 반환된다
            assertThat(foundUser).isNull();
        }
    }
}
