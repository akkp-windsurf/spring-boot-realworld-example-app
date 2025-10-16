package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.application.TagsQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@WebMvcTest(TagsApi.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonCustomizations.class)
public class TagsApiTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private TagsQueryService tagsQueryService;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    public void should_get_all_tags_success() {
        when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "boot"));

        given()
            .when()
            .get("/tags")
            .then()
            .statusCode(200)
            .body("tags", hasSize(3))
            .body("tags", contains("java", "spring", "boot"));
    }

    @Test
    public void should_return_empty_tags_when_no_tags_exist() {
        when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

        given()
            .when()
            .get("/tags")
            .then()
            .statusCode(200)
            .body("tags", empty());
    }

    @Test
    public void should_get_single_tag() {
        when(tagsQueryService.allTags()).thenReturn(Collections.singletonList("java"));

        given()
            .when()
            .get("/tags")
            .then()
            .statusCode(200)
            .body("tags", hasSize(1))
            .body("tags[0]", org.hamcrest.core.IsEqual.equalTo("java"));
    }

    @Test
    public void should_get_tags_with_special_characters() {
        when(tagsQueryService.allTags()).thenReturn(Arrays.asList("c++", "c#", "node.js"));

        given()
            .when()
            .get("/tags")
            .then()
            .statusCode(200)
            .body("tags", hasSize(3))
            .body("tags", contains("c++", "c#", "node.js"));
    }
}
