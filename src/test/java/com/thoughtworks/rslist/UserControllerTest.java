package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.api.UserController;
import com.thoughtworks.rslist.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class UserControllerTest {

    private static final String ROOT_URL = "/user";

    private MockMvc mockMvc;

    private List<User> initialData;

    @BeforeEach
    void init() {
        initialData = new ArrayList<User>() {{
            add(new User("Alice", 20, "female", "alice@tw.com", "13000000000"));
            add(new User("Bob", 22, "male", "bob@tw.com", "15111111111"));
            add(new User("Charlie", 25, "female", "charlie@tw.com", "18222222222"));
        }};
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController()).build();
    }

    @Test
    void should_get_all_users() throws Exception {

        String serializedExpectedResult = new ObjectMapper().writeValueAsString(initialData);

        mockMvc.perform(get(ROOT_URL + "/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(initialData.size())))
                .andExpect(result -> assertEquals(serializedExpectedResult,
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8)));
    }

    @Test
    void should_add_user() throws Exception {

        User dave = new User("Dave", 28, "male", "dave@tw.com", "19333333333");
        String serialized = new ObjectMapper().writeValueAsString(dave);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isCreated())
                .andExpect(header().string("index", any(String.class)));
    }

}
