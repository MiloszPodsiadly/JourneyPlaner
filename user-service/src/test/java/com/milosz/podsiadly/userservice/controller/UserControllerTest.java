package com.milosz.podsiadly.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.userservice.dto.CreateUserRequest;
import com.milosz.podsiadly.userservice.service.UserService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("✅ UserController WebMvc Test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class Config {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    private CreateUserRequest request;

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Test setup...");
        request = new CreateUserRequest("spotify:999", "Test User", "test@example.com");
    }

    @AfterEach
    void tearDown() {
        request = null;
        System.out.println("⬅️ [AfterEach] Cleaning up...");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserController tests completed.");
    }

    @Test
    void shouldCreateUserIfNotExists() throws Exception {
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).createUserIfNotExists(any());
    }
}