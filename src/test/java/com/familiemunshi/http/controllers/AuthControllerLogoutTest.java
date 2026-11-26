package com.familiemunshi.http.controllers;

import com.familiemunshi.common.service.TokenRevocationService;
import com.familiemunshi.common.utils.JwtUtil;
import com.familiemunshi.http.dtos.requests.LogoutRequest;
import com.familiemunshi.http.dtos.requests.RefreshTokenRequest;
import com.familiemunshi.service.AuthService;
import com.familiemunshi.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerLogoutTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @InjectMocks
    private AuthController authController;

    private static MockHttpServletRequest requestWithToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        return request;
    }

    @Test
    void logout_noBody_revokesOnlyAccessToken() {
        when(jwtUtil.getRemainingMillis("access-token")).thenReturn(5_000L);

        authController.logout(requestWithToken(), null);

        verify(tokenRevocationService).revoke("access-token", 5_000L);
        verify(tokenRevocationService, never()).revoke(org.mockito.ArgumentMatchers.argThat(t -> !t.equals("access-token")), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void logout_withRefreshToken_revokesBothTokens() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");
        when(jwtUtil.getRemainingMillis("access-token")).thenReturn(5_000L);
        when(jwtUtil.getRemainingMillis("refresh-token")).thenReturn(600_000L);

        var response = authController.logout(requestWithToken(), request);

        verify(tokenRevocationService).revoke("access-token", 5_000L);
        verify(tokenRevocationService).revoke("refresh-token", 600_000L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void logout_blankRefreshToken_revokesOnlyAccessToken() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("   ");
        when(jwtUtil.getRemainingMillis("access-token")).thenReturn(5_000L);

        authController.logout(requestWithToken(), request);

        verify(tokenRevocationService).revoke("access-token", 5_000L);
        verify(tokenRevocationService, never()).revoke("   ", 0L);
    }

    @Test
    void refresh_revokedRefreshToken_throwsUnauthorized() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("revoked-refresh-token");
        when(jwtUtil.validateUserRefreshToken("revoked-refresh-token")).thenReturn(true);
        when(tokenRevocationService.isRevoked("revoked-refresh-token")).thenReturn(true);

        assertThatThrownBy(() -> authController.refresh(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid or expired refresh token");

        verify(userService, never()).getUser(org.mockito.ArgumentMatchers.any());
    }
}
