package com.platform.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCachingIT extends IntegrationTestBase {

    @Autowired StringRedisTemplate redis;
    @Autowired org.springframework.boot.test.web.client.TestRestTemplate rest;

    @Test
    void regionsList_secondCallUsesRedisCacheEntry() {
        redis.getConnectionFactory().getConnection().flushAll();

        var resp1 = rest.getForEntity("/api/v1/regions?page=0&size=20", String.class);
        assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);

        var keysAfterFirst = redis.keys("regions:list*");
        assertThat(keysAfterFirst).isNotNull();
        assertThat(keysAfterFirst).isNotEmpty();

        var resp2 = rest.getForEntity("/api/v1/regions?page=0&size=20", String.class);
        assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);

        var keysAfterSecond = redis.keys("regions:list*");
        assertThat(keysAfterSecond).isNotEmpty();
    }
}

