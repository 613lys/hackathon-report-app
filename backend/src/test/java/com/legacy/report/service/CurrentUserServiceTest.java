package com.legacy.report.service;

import com.legacy.report.model.User;
import com.legacy.report.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    private User maker;

    @BeforeEach
    void setUp() {
        maker = new User();
        maker.setUsername("maker1");
        maker.setRole("MAKER,CHECKER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserOrThrowReturnsUserFromRepository() {
        mockAuthentication("maker1");
        when(userRepository.findByUsername("maker1")).thenReturn(Optional.of(maker));

        User result = currentUserService.getCurrentUserOrThrow();

        assertThat(result).isSameAs(maker);
    }

    @Test
    void getCurrentUserOrThrowThrowsWhenUserMissing() {
        mockAuthentication("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> currentUserService.getCurrentUserOrThrow());
    }

    @Test
    void requireRolePassesWhenUserHasRole() {
        currentUserService.requireRole(maker, "MAKER");
        assertThat(currentUserService.hasRole(maker, "MAKER")).isTrue();
    }

    @Test
    void requireRoleThrowsWhenMissingRole() {
        maker.setRole("CHECKER");
        assertThrows(RuntimeException.class, () -> currentUserService.requireRole(maker, "MAKER"));
    }

    private void mockAuthentication(String username) {
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(username, null));
        SecurityContextHolder.setContext(context);
    }
}
