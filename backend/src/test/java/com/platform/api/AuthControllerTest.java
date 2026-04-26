package com.platform.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.dto.AuthDtos;
import com.platform.api.dto.UserDto;
import com.platform.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.platform.api.controller.AuthController;
import com.platform.api.controller.GlobalExceptionHandler;
import com.platform.infrastructure.config.SecurityConfig;
import com.platform.infrastructure.security.JwtAuthenticationFilter;
import com.platform.infrastructure.security.JwtService;
import com.platform.infrastructure.security.OAuth2SuccessHandler;
import com.platform.service.UserDetailsServiceImpl;

@WebMvcTest(controllers = AuthController.class)
@Import({GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean OAuth2SuccessHandler oAuth2SuccessHandler;
    @MockBean com.platform.domain.repository.UserRepository userRepository;
    @MockBean com.platform.domain.repository.RoleRepository roleRepository;

    @Test
    void register_withValidRequest_shouldReturn201() throws Exception {
        UserDto userDto = new UserDto(1L, "test@example.com", "Test User", "USER", true, LocalDateTime.now());
        AuthDtos.AuthResponse response = AuthDtos.AuthResponse.of("token", "refresh", 86400L, userDto);
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthDtos.RegisterRequest("test@example.com", "Password1!", "Test User"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("token"));
    }

    @Test
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthDtos.RegisterRequest("not-an-email", "Password1!", "Test"))))
                .andExpect(status().isBadRequest());
    }
}
