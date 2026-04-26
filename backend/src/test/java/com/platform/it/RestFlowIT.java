package com.platform.it;

import com.platform.api.dto.AuthDtos;
import com.platform.api.dto.ClusteringDtos;
import com.platform.api.dto.RegionDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestFlowIT extends IntegrationTestBase {

    @Autowired org.springframework.boot.test.web.client.TestRestTemplate rest;

    @Test
    void createRegion_runClustering_fetchRunDetails() {
        // login as seeded admin (Liquibase seed)
        AuthDtos.LoginRequest login = new AuthDtos.LoginRequest("admin@platform.com", "Admin1234!");
        ResponseEntity<AuthDtos.AuthResponse> authResp = rest.postForEntity("/api/v1/auth/login", login, AuthDtos.AuthResponse.class);
        assertThat(authResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = authResp.getBody().accessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // create a region (admin endpoint)
        RegionDtos.CreateRegionRequest createRegion = new RegionDtos.CreateRegionRequest(
                "IT-RM", "Rome", "IT", null, null, 1_000_000L, null
        );
        ResponseEntity<RegionDtos.RegionDto> created = rest.exchange(
                "/api/v1/regions", HttpMethod.POST, new HttpEntity<>(createRegion, headers), RegionDtos.RegionDto.class);
        assertThat(created.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.CONFLICT); // idempotent across runs

        // list indicators and pick 1
        ResponseEntity<com.platform.api.dto.IndicatorDtos.IndicatorDto[]> indicators =
                rest.exchange("/api/v1/indicators", HttpMethod.GET, new HttpEntity<>(headers), com.platform.api.dto.IndicatorDtos.IndicatorDto[].class);
        assertThat(indicators.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(indicators.getBody()).isNotNull();
        assertThat(indicators.getBody().length).isGreaterThan(0);

        Long indicatorId = indicators.getBody()[0].id();

        // run clustering
        ClusteringDtos.ClusteringRequest clusteringRequest = new ClusteringDtos.ClusteringRequest(
                "IT flow test", 2, 2022, List.of(indicatorId)
        );
        ResponseEntity<ClusteringDtos.ClusteringRunDto> runResp = rest.exchange(
                "/api/v1/clustering/run", HttpMethod.POST, new HttpEntity<>(clusteringRequest, headers), ClusteringDtos.ClusteringRunDto.class);
        assertThat(runResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(runResp.getBody()).isNotNull();
        Long runId = runResp.getBody().id();

        // fetch run details
        ResponseEntity<ClusteringDtos.ClusteringRunDto> fetched = rest.exchange(
                "/api/v1/clustering/" + runId, HttpMethod.GET, new HttpEntity<>(headers), ClusteringDtos.ClusteringRunDto.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().assignments()).isNotEmpty();
    }
}

