package io.spring.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class RestApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void should_complete_full_api_workflow() {
        Map<String, Object> registerRequest = new HashMap<>();
        Map<String, Object> userRegister = new HashMap<>();
        userRegister.put("email", "integration@test.com");
        userRegister.put("username", "integrationuser");
        userRegister.put("password", "password123");
        registerRequest.put("user", userRegister);

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "/users",
            registerRequest,
            String.class
        );
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertTrue(registerResponse.getBody().contains("integration@test.com"));
        assertTrue(registerResponse.getBody().contains("integrationuser"));
        assertTrue(registerResponse.getBody().contains("token"));

        Map<String, Object> loginRequest = new HashMap<>();
        Map<String, Object> userLogin = new HashMap<>();
        userLogin.put("email", "integration@test.com");
        userLogin.put("password", "password123");
        loginRequest.put("user", userLogin);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "/users/login",
            loginRequest,
            String.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertTrue(loginResponse.getBody().contains("token"));
        
        String token = loginResponse.getBody().split("\"token\":\"")[1].split("\"")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> currentUserResponse = restTemplate.exchange(
            "/user",
            HttpMethod.GET,
            entity,
            String.class
        );
        assertEquals(HttpStatus.OK, currentUserResponse.getStatusCode());
        assertNotNull(currentUserResponse.getBody());
        assertTrue(currentUserResponse.getBody().contains("integrationuser"));

        Map<String, Object> articleRequest = new HashMap<>();
        Map<String, Object> article = new HashMap<>();
        article.put("title", "Integration Test Article");
        article.put("description", "Testing the REST API");
        article.put("body", "This article verifies that the API works correctly after upgrading to Java 17 and Spring Boot 3");
        article.put("tagList", new String[]{"test", "integration"});
        articleRequest.put("article", article);

        HttpEntity<Map<String, Object>> articleEntity = new HttpEntity<>(articleRequest, headers);
        ResponseEntity<String> createArticleResponse = restTemplate.exchange(
            "/articles",
            HttpMethod.POST,
            articleEntity,
            String.class
        );
        assertEquals(HttpStatus.OK, createArticleResponse.getStatusCode());
        assertNotNull(createArticleResponse.getBody());
        assertTrue(createArticleResponse.getBody().contains("Integration Test Article"));
        
        String articleSlug = createArticleResponse.getBody().split("\"slug\":\"")[1].split("\"")[0];

        ResponseEntity<String> listArticlesResponse = restTemplate.getForEntity(
            "/articles",
            String.class
        );
        assertEquals(HttpStatus.OK, listArticlesResponse.getStatusCode());
        assertNotNull(listArticlesResponse.getBody());
        assertTrue(listArticlesResponse.getBody().contains("articles"));

        ResponseEntity<String> favoriteResponse = restTemplate.exchange(
            "/articles/" + articleSlug + "/favorite",
            HttpMethod.POST,
            entity,
            String.class
        );
        assertEquals(HttpStatus.OK, favoriteResponse.getStatusCode());
        assertNotNull(favoriteResponse.getBody());
        assertTrue(favoriteResponse.getBody().contains("\"favorited\":true"));

        ResponseEntity<String> feedResponse = restTemplate.exchange(
            "/articles/feed",
            HttpMethod.GET,
            entity,
            String.class
        );
        assertEquals(HttpStatus.OK, feedResponse.getStatusCode());
        assertNotNull(feedResponse.getBody());
        assertTrue(feedResponse.getBody().contains("articles"));
    }
}
