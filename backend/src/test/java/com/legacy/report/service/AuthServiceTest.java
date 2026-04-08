package com.legacy.report.service;

import com.legacy.report.model.User;
import com.legacy.report.repository.UserRepository;
import com.legacy.report.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User maker;

    @BeforeEach
    void setUp() {
        maker = new User();
        maker.setUsername("maker1");
        maker.setPassword("hashed");
    }

    @Test
    void loginReturnsJwtTokenWhenPasswordMatches() {
        when(userRepository.findByUsername("maker1")).thenReturn(Optional.of(maker));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(jwtTokenProvider.createToken("maker1")).thenReturn("jwt-token");

        String token = authService.login("maker1", "123456");

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void loginThrowsWhenPasswordMismatch() {
        when(userRepository.findByUsername("maker1")).thenReturn(Optional.of(maker));
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login("maker1", "bad"));
    }

    @Test
    void loginThrowsWhenUserMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login("ghost", "123"));
    }

    @Test
    void findByUsernameDelegatesToRepository() {
        when(userRepository.findByUsername("maker1")).thenReturn(Optional.of(maker));

        User result = authService.findByUsername("maker1");

        assertThat(result).isSameAs(maker);
    }
}
