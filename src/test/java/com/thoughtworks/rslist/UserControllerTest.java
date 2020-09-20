package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    private static final boolean BULK_MODE = true;

    private static final String ROOT_URL = "/user";

    private static List<User> existingUsers;
    private static User newUser;

    private static int pageIndex;
    private static int pageSize;

    @Autowired
    private MockMvc mockMvc;

    private final UserRepository userRepository;

    @Autowired
    public UserControllerTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @BeforeAll
    static void beforeAll() {
        newUser = new User("newuser", 28, "male", "newuser@tw.com", "18888888888");
        pageIndex = 1;
        pageSize = 10;
    }

    @BeforeEach
    void setUp() {
        if (BULK_MODE) return;
        init();
    }

    @Test
    @Order(0)
    void setUpForBulkMode() {
        if (!BULK_MODE) return;
        init();
    }

    private void init() {
        Pageable pageable = PageRequest.of(pageIndex - 1, pageSize);
        Page<UserPO> page = userRepository.findAll(pageable);
        existingUsers = new ArrayList<>();
        page.forEach(userPO -> existingUsers.add(new User(userPO)));
    }

    private void setUpAndTearDownForAdd() {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            userRepository.deleteByUsername(newUser.getUsername());
        }
    }

    @Test
    @Order(1)
    void should_add_user_get_returned_id() throws Exception {

        setUpAndTearDownForAdd();

        long countBefore = userRepository.count();

        String serialized = new ObjectMapper().writeValueAsString(newUser);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isCreated())
                .andExpect(header().string("id", any(String.class)))
                .andDo(result -> {
                    String headerId = result.getResponse().getHeader("id");
                    if (headerId != null) {
                        newUser.setId(Long.parseLong(headerId));
                    } else {
                        fail();
                    }
                });

        assertEquals(countBefore + 1, userRepository.count());

        setUpAndTearDownForAdd();
    }

    @Test
    @Order(2)
    void should_get_username_exists_error_when_add_user_given_existing_username() throws Exception {

        Pageable pageable = PageRequest.of(0, 1);
        Page<UserPO> page = userRepository.findAll(pageable);

        String existingUsername = page.stream().collect(Collectors.toList()).get(0).getUsername();
        User existing = new User(newUser);
        existing.setUsername(existingUsername);

        String serialized = new ObjectMapper().writeValueAsString(existing);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(String.format("username %s already exists", existingUsername))));
    }

    private void setUpForDelete() {
        boolean result = !userRepository.existsByUsername(newUser.getUsername());
        if (result) {
            UserPO newUserPO = UserPO.builder()
                    .username(newUser.getUsername())
                    .gender(newUser.getGender())
                    .age(newUser.getAge())
                    .email(newUser.getEmail())
                    .phone(newUser.getPhone())
                    .votes(10)
                    .build();
            userRepository.save(newUserPO);
            newUser.setId(newUserPO.getId());
            newUser.setVotes(newUserPO.getVotes());
        }
    }

    @Test
    @Order(3)
    void should_delete_user_given_id() throws Exception {

        setUpForDelete();

        assertNotNull(newUser.getId());
        long id = newUser.getId();

        long countBefore = userRepository.count();

        mockMvc.perform(delete(ROOT_URL)
                .param("id", Long.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isOk());

        assertEquals(countBefore - 1, userRepository.count());
    }

    @Test
    @Order(4)
    void should_get_not_found_when_delete_given_non_existent_id() throws Exception {

        long id = 99999999L;

        mockMvc.perform(delete(ROOT_URL)
                .param("id", Long.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_single_user_by_id() throws Exception {

        User user = existingUsers.get(0);
        long id = userRepository.findByUsername(user.getUsername()).getId();

        ResultActions resultActions = mockMvc.perform(
                get(ROOT_URL).param("id", Long.toString(id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateSingleUserResult(resultActions, user);
    }

    @Test
    void should_get_single_user_by_username() throws Exception {

        User user = existingUsers.get(0);

        ResultActions resultActions = mockMvc.perform(
                get(ROOT_URL).param("username", user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateSingleUserResult(resultActions, user);
    }

    @Test
    void should_get_single_user_by_both() throws Exception {

        User user = existingUsers.get(0);
        long id = userRepository.findByUsername(user.getUsername()).getId();

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL)
                .param("username", user.getUsername())
                .param("id", Long.toString(id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateSingleUserResult(resultActions, user);
    }

    @Test
    void should_get_not_found_error_by_both() throws Exception {

        User user = existingUsers.get(0);
        long id = 99999999L;

        mockMvc.perform(get(ROOT_URL)
                .param("username", user.getUsername())
                .param("id", Long.toString(id)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_all_users() throws Exception {

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL + "/list")
                .param("pageSize", "10")
                .param("pageIndex", "1"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(existingUsers.size())));

        validateListUserResult(resultActions, existingUsers);
    }

    void validateSingleUserResult(ResultActions resultActions, User user) throws Exception {
        resultActions
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.age",      is(user.getAge())))
                .andExpect(jsonPath("$.gender",   is(user.getGender())))
                .andExpect(jsonPath("$.email",    is(user.getEmail())))
                .andExpect(jsonPath("$.phone",    is(user.getPhone())))
                .andExpect(jsonPath("$.votes",    is(user.getVotes())));
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
                    .andExpect(jsonPath(String.format("$[%d].votes"   , i), is(user.getVotes())));
        }
    }

    @Test
    void should_get_invalid_param_error_when_add_user_given_wrong_user_param() throws Exception {

        newUser.setPhone(null);
        String serialized = new ObjectMapper().writeValueAsString(newUser);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("invalid param")));
    }

}
