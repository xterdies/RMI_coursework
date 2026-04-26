package com.platform.api;

import com.platform.infrastructure.config.SecurityConfig;
import com.platform.infrastructure.security.JwtAuthenticationFilter;
import com.platform.infrastructure.security.JwtService;
import com.platform.infrastructure.security.OAuth2SuccessHandler;
import com.platform.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CorsConfigTest {

    @Test
    void corsAllowedOrigins_defaultPropertyIncludesLocalhostVite() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(JwtAuthenticationFilter.class, () -> mock(JwtAuthenticationFilter.class))
                .withBean(UserDetailsService.class, () -> mock(UserDetailsServiceImpl.class))
                .withBean(OAuth2SuccessHandler.class, () -> mock(OAuth2SuccessHandler.class))
                .withBean(JwtService.class, () -> mock(JwtService.class))
                .withBean(SecurityConfig.class);

        runner.withPropertyValues(
                        "app.cors.allowed-origins=http://localhost:5173,http://localhost,http://localhost:80"
                )
                .run(ctx -> {
                    SecurityConfig config = ctx.getBean(SecurityConfig.class);
                    var source = config.corsConfigurationSource();
                    var cors = source.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/regions"));
                    assertThat(cors).isNotNull();
                    assertThat(cors.getAllowedOrigins()).contains("http://localhost:5173", "http://localhost", "http://localhost:80");
                });
    }
}

