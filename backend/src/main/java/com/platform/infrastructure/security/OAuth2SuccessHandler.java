package com.platform.infrastructure.security;

import com.platform.domain.entity.Role;
import com.platform.domain.entity.User;
import com.platform.domain.repository.RoleRepository;
import com.platform.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Всегда используем наш целевой URL, игнорируем сохранённые запросы
        setDefaultTargetUrl("http://localhost/oauth2/callback");
        setAlwaysUseDefaultTargetUrl(true);
        clearAuthenticationAttributes(request);

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String subject = oidcUser.getSubject();

        User user = userRepository.findByOauthProviderAndOauthSubject("google", subject)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseGet(() -> createOAuthUser(email, name, subject)));

        String token = jwtService.generateToken(user);
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost/oauth2/callback")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User createOAuthUser(String email, String name, String subject) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found"));
        return userRepository.save(User.builder()
                .email(email)
                .fullName(name)
                .oauthProvider("google")
                .oauthSubject(subject)
                .role(userRole)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }
}