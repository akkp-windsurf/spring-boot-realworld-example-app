package io.spring.infrastructure.service;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultJwtServiceTest {

    private JwtService jwtService;

    @Before
    public void setUp() {
        jwtService = new DefaultJwtService("test-secret-key-for-jwt-signing-must-be-at-least-512-bits-long-12345", 3600);
    }

    @Test
    public void should_generate_and_parse_token() {
        User user = new User("email@email.com", "username", "123", "", "");
        String token = jwtService.toToken(user);
        assertNotNull(token);
        Optional<String> optional = jwtService.getSubFromToken(token);
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), user.getId());
    }

    @Test
    public void should_get_null_with_wrong_jwt() {
        Optional<String> optional = jwtService.getSubFromToken("123");
        assertFalse(optional.isPresent());
    }

    @Test
    public void should_get_null_with_expired_jwt() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhaXNlbnNpeSIsImV4cCI6MTUwMjE2MTIwNH0.SJB-U60WzxLYNomqLo4G3v3LzFxJKuVrIud8D8Lz3-mgpo9pN1i7C8ikU_jQPJGm8HsC1CquGMI-rSuM7j6LDA";
        assertFalse(jwtService.getSubFromToken(token).isPresent());
    }

    @Test
    public void should_handle_null_token_gracefully() {
        Optional<String> optional = jwtService.getSubFromToken(null);
        assertFalse(optional.isPresent());
    }

    @Test
    public void should_handle_empty_token() {
        Optional<String> optional = jwtService.getSubFromToken("");
        assertFalse(optional.isPresent());
    }

    @Test
    public void should_handle_malformed_token() {
        Optional<String> optional = jwtService.getSubFromToken("not.a.jwt.token");
        assertFalse(optional.isPresent());
    }

    @Test
    public void should_handle_token_without_signature() {
        Optional<String> optional = jwtService.getSubFromToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0In0");
        assertFalse(optional.isPresent());
    }

    @Test
    public void should_generate_token_with_short_expiration() {
        JwtService shortExpirationService = new DefaultJwtService("test-secret-key-for-jwt-signing-must-be-at-least-512-bits-long-12345", 1);
        User user = new User("email@email.com", "username", "123", "", "");
        String token = shortExpirationService.toToken(user);
        assertNotNull(token);
        Optional<String> optional = shortExpirationService.getSubFromToken(token);
        assertTrue(optional.isPresent());
    }

    @Test
    public void should_work_with_production_length_secret() {
        String longSecret = "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA";
        JwtService prodLikeService = new DefaultJwtService(longSecret, 3600);
        User user = new User("email@email.com", "username", "456", "", "");
        String token = prodLikeService.toToken(user);
        assertNotNull(token);
        Optional<String> optional = prodLikeService.getSubFromToken(token);
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), user.getId());
    }

    @Test
    public void should_generate_different_tokens_for_different_users() {
        User user1 = new User("email1@email.com", "username1", "password1", "", "");
        User user2 = new User("email2@email.com", "username2", "password2", "", "");
        
        String token1 = jwtService.toToken(user1);
        String token2 = jwtService.toToken(user2);
        
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        
        assertEquals(user1.getId(), jwtService.getSubFromToken(token1).get());
        assertEquals(user2.getId(), jwtService.getSubFromToken(token2).get());
    }

    @Test
    public void should_not_validate_token_with_wrong_secret() {
        User user = new User("email@email.com", "username", "123", "", "");
        String token = jwtService.toToken(user);
        
        JwtService differentSecretService = new DefaultJwtService("different-secret-key-for-jwt-signing-must-be-at-least-512-bits-9999", 3600);
        Optional<String> optional = differentSecretService.getSubFromToken(token);
        
        assertFalse(optional.isPresent());
    }
}
