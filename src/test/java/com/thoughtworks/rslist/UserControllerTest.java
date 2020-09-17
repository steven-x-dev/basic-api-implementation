package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    private static final String ROOT_URL = "/user";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private List<User> initialUsers;
    private User dave;

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        initialUsers = new ArrayList<User>() {{
            add(new User("Alice", 20, "female", "alice@tw.com", "13000000000"));
            add(new User("Bob", 22, "male", "bob@tw.com", "15111111111"));
            add(new User("Charlie", 25, "female", "charlie@tw.com", "18222222222"));
        }};
        dave = new User("Dave", 28, "male", "dave@tw.com", "19333333333");
        initialUsers.forEach(u -> {
            Exception exception = null;
            try {
                should_add_user(u);
            } catch (Exception e) {
                exception = e;
            }
            assertNull(exception);
        });
    }

    @Test
    void should_add_user() throws Exception {
        should_add_user(dave);
    }

    private void should_add_user(User user) throws Exception {

        long countBefore = userRepository.count();

        String serialized = new ObjectMapper().writeValueAsString(user);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isCreated())
                .andExpect(header().string("id", any(String.class)));

        assertEquals(countBefore + 1, userRepository.count());
    }

    @Test
    void should_get_username_exists_error_when_add_user_given_existing_username() throws Exception {

        String existingUsername = userRepository.findAll().iterator().next().getUsername();
        dave.setUsername(existingUsername);

        String serialized = new ObjectMapper().writeValueAsString(dave);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(String.format("username %s is used", existingUsername))));
    }

    @Test
    void should_get_all_users() throws Exception {

        String serializedExpectedResult = new ObjectMapper().writeValueAsString(initialUsers);

        mockMvc.perform(get(ROOT_URL + "/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(initialUsers.size())))
                .andExpect(result -> assertEquals(serializedExpectedResult,
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8)));
    }

    @Test
    void should_get_invalid_user_error_when_add_user_given_wrong_user_param() throws Exception {

        dave.setPhone(null);
        String serialized = new ObjectMapper().writeValueAsString(dave);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

}
