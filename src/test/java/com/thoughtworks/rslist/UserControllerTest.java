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
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    private static final String ROOT_URL = "/user";

    @Autowired
    private MockMvc mockMvc;

    private final UserRepository userRepository;

    @Autowired
    public UserControllerTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private List<User> initialUsers;
    private User dave;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        initialUsers = new ArrayList<User>() {{
            add(new User("Alice", 20, "female", "alice@tw.com", "13000000000"));
            add(new User("Bob", 22, "male", "bob@tw.com", "15111111111"));
            add(new User("Charlie", 25, "male", "charlie@tw.com", "18222222222"));
        }};
        dave = new User("Dave", 28, "male", "dave@tw.com", "19333333333");
        initialUsers.forEach(u -> {
            Exception exception = null;
            try {
                addUser(u);
            } catch (Exception e) {
                exception = e;
            }
            assertNull(exception);
        });
    }

    @Test
    void should_add_user() throws Exception {

        long countBefore = userRepository.count();

        addUser(dave)
                .andExpect(status().isCreated())
                .andExpect(header().string("id", any(String.class)));

        assertEquals(countBefore + 1, userRepository.count());
    }

    private ResultActions addUser(User user) throws Exception {

        String serialized = new ObjectMapper().writeValueAsString(user);

        return mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized));
    }

    @Test
    void should_get_username_exists_error_when_add_user_given_existing_username() throws Exception {

        String existingUsername = userRepository.findAll().get(0).getUsername();
        dave.setUsername(existingUsername);

        String serialized = new ObjectMapper().writeValueAsString(dave);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(String.format("username %s already exists", existingUsername))));
    }

    @Test
    void should_get_single_user_by_id() throws Exception {

        User user = initialUsers.get(0);
        int id = userRepository.findByUsername(user.getUsername()).getId();

        ResultActions resultActions = mockMvc.perform(
                get(ROOT_URL).param("id", Integer.toString(id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateSingleUserResult(resultActions, user);
    }

    @Test
    void should_get_single_user_by_username() throws Exception {

        User user = initialUsers.get(0);

        ResultActions resultActions = mockMvc.perform(
                get(ROOT_URL).param("username", user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateSingleUserResult(resultActions, user);
    }

    @Test
    void should_get_single_user_by_both() throws Exception {

        User user = initialUsers.get(0);
        int id = userRepository.findByUsername(user.getUsername()).getId();

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL)
                .param("username", user.getUsername())
                .param("id", Integer.toString(id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateSingleUserResult(resultActions, user);
    }

    @Test
    void should_get_not_found_error_by_both() throws Exception {

        User user = initialUsers.get(0);
        int id = 99999999;

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL)
                .param("username", user.getUsername())
                .param("id", Integer.toString(id)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_all_users() throws Exception {

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL + "/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(initialUsers.size())));

        validateListUserResult(resultActions, initialUsers);
    }

    void validateSingleUserResult(ResultActions resultActions, User user) throws Exception {
        resultActions
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.age",      is(user.getAge())))
                .andExpect(jsonPath("$.gender",   is(user.getGender())))
                .andExpect(jsonPath("$.email",    is(user.getEmail())))
                .andExpect(jsonPath("$.phone",    is(user.getPhone())))
                .andExpect(jsonPath("$.votes",    is(10)));
    }

    void validateListUserResult(ResultActions resultActions, List<User> users) throws Exception {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            resultActions
                    .andExpect(jsonPath(String.format("$[%d].username", i), is(user.getUsername())))
                    .andExpect(jsonPath(String.format("$[%d].age"     , i), is(user.getAge())))
                    .andExpect(jsonPath(String.format("$[%d].gender"  , i), is(user.getGender())))
                    .andExpect(jsonPath(String.format("$[%d].email"   , i), is(user.getEmail())))
                    .andExpect(jsonPath(String.format("$[%d].phone"   , i), is(user.getPhone())))
                    .andExpect(jsonPath(String.format("$[%d].votes"   , i), is(10)));
        }
    }

    @Test
    void should_get_invalid_param_error_when_add_user_given_wrong_user_param() throws Exception {

        dave.setPhone(null);
        String serialized = new ObjectMapper().writeValueAsString(dave);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("invalid param")));
    }

    @Test
    void should_delete_user_given_id() throws Exception {

        int index = 0;
        int id = userRepository.findByUsername(initialUsers.get(index).getUsername()).getId();

        mockMvc.perform(delete(ROOT_URL)
                .param("id", Integer.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isOk());
    }

    @Test
    void should_get_not_found_when_delete_given_non_existent_id() throws Exception {

        int id = 99999999;

        mockMvc.perform(delete(ROOT_URL)
                .param("id", Integer.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isNotFound());
    }

}
