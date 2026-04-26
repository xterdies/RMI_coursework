package com.platform.it;

import com.platform.api.dto.AuthDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFlowIT extends IntegrationTestBase {

    @Autowired TestRestTemplate rest;

    @Test
    void login_withSeededAdmin_canAccessProtectedEndpoint() {
        AuthDtos.LoginRequest login = new AuthDtos.LoginRequest("admin@platform.com", "Admin1234!");
        ResponseEntity<AuthDtos.AuthResponse> authResp = rest.postForEntity("/api/v1/auth/login", login, AuthDtos.AuthResponse.class);
        assertThat(authResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authResp.getBody()).isNotNull();
        assertThat(authResp.getBody().accessToken()).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authResp.getBody().accessToken());
        ResponseEntity<String> regionsResp = rest.exchange("/api/v1/regions?page=0&size=1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(regionsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(regionsResp.getBody()).contains("content");
    }
}

