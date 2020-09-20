package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RsEventControllerTest {

    private static final boolean BULK_MODE = true;

    private static final String ROOT_URL = "/rs";

    private static String defaultUsername;
    private static User defaultUser;
    private static List<RsEvent> existingEvents;
    private static RsEvent newEvent;

    @Autowired
    private MockMvc mockMvc;

    private final RsEventRepository rsEventRepository;

    private final UserRepository userRepository;

    @Autowired
    public RsEventControllerTest(RsEventRepository rsEventRepository, UserRepository userRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
    }

    @BeforeAll
    static void beforeAll() {
        defaultUsername = "rimowa";
        newEvent = new RsEvent("第四条热搜", "娱乐", null);
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
        UserPO defaultUserPO = userRepository.findByUsername(defaultUsername);

        assertNotNull(defaultUserPO);
        defaultUser = new User(defaultUserPO);

        newEvent.setUserId(defaultUser.getId());

        existingEvents = new ArrayList<>();
        rsEventRepository.findAll().forEach(rsEventPO -> existingEvents.add(new RsEvent(rsEventPO)));
    }

    private void setUpAndTearDownForAdd() {
        if (rsEventRepository.existsByEventName(newEvent.getEventName())) {
            rsEventRepository.deleteByEventName(newEvent.getEventName());
        }
    }

    @Test
    @Order(1)
    void should_add_rs_event_and_get_returned_id() throws Exception {

        setUpAndTearDownForAdd();

        long countBefore = rsEventRepository.count();

        String serialized = new ObjectMapper().writeValueAsString(newEvent);

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
                        newEvent.setId(Long.parseLong(headerId));
                    } else {
                        fail();
                    }
                });

        assertEquals(countBefore + 1, rsEventRepository.count());

        setUpAndTearDownForAdd();
    }

    @Test
    @Order(2)
    void should_get_invalid_param_error_when_add_event_given_wrong_param() throws Exception {

        RsEvent added = new RsEvent(newEvent.getEventName(), null, defaultUser.getId());

        String serialized = new ObjectMapper().writeValueAsString(added);

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
    @Order(3)
    void should_get_event_name_exists_error_when_add_event_given_existing_event_name() throws Exception {

        RsEvent added = new RsEvent(existingEvents.get(0).getEventName(), newEvent.getKeyword(), defaultUser.getId());

        String serialized = new ObjectMapper().writeValueAsString(added);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(String.format("event name %s already exists", added.getEventName()))));
    }

    @Test
    @Order(4)
    void should_get_user_not_exists_error_when_add_event_given_non_existent_user_id() throws Exception {

        RsEvent added = new RsEvent(newEvent.getEventName(), newEvent.getKeyword(), 99999999L);
        String serialized = new ObjectMapper().writeValueAsString(added);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("user does not exist")));
    }

    private void setUpForDelete() {
        if (!rsEventRepository.existsByEventName(newEvent.getEventName())) {
            long defaultUserId = defaultUser.getId();
            UserPO defaultUserPO = userRepository.findById(defaultUserId);
            RsEventPO newEventPO = RsEventPO.builder()
                    .eventName(newEvent.getEventName())
                    .keyword(newEvent.getKeyword())
                    .userPO(defaultUserPO)
                    .build();
            rsEventRepository.save(newEventPO);
            newEvent.setId(newEventPO.getId());
        }
    }

    @Test
    @Order(5)
    void should_delete_event_given_id() throws Exception {

        setUpForDelete();

        assertNotNull(newEvent.getId());
        long id = newEvent.getId();

        long countBefore = rsEventRepository.count();

        mockMvc.perform(delete(ROOT_URL)
                .param("id", Long.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isOk());

        assertEquals(countBefore - 1, rsEventRepository.count());
    }

    @Test
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
    void should_find_one_rs_event_by_event_name() throws Exception {
        for (RsEvent event : existingEvents) {
            ResultActions resultActions = mockMvc.perform(
                    get(ROOT_URL).param("eventName", event.getEventName()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
            validateSingleRsEventResult(resultActions, event);
        }
    }

    @Test
    void should_get_invalid_index_error_when_find_one_rs_event_given_non_existent_id() throws Exception {
        long id = 99999999L;
        mockMvc.perform(
                get(ROOT_URL).param("id", Long.toString(id)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_list_all_rs_events() throws Exception {

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL + "/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(existingEvents.size())));

        validateListRsEventResult(resultActions, existingEvents);
    }

    void validateSingleRsEventResult(ResultActions resultActions, RsEvent event) throws Exception {
        resultActions
                .andExpect(jsonPath("$.eventName", is(event.getEventName())))
                .andExpect(jsonPath("$.keyword"  , is(event.getKeyword())))
                .andExpect(jsonPath("$.userId"   , is(event.getUserId()), Long.class));
    }

    void validateListRsEventResult(ResultActions resultActions, List<RsEvent> events) throws Exception {
        for (int i = 0; i < events.size(); i++) {
            RsEvent event = events.get(i);
            resultActions
                    .andExpect(jsonPath(String.format("$[%d].eventName", i), is(event.getEventName())))
                    .andExpect(jsonPath(String.format("$[%d].keyword"  , i), is(event.getKeyword())))
                    .andExpect(jsonPath(String.format("$[%d].userId"   , i), is(event.getUserId()), Long.class));
        }
    }

    @Test
    void should_update_one_rs_event_when_supplying_event_name_given_id() throws Exception {

        int index = 2;

        ObjectMapper objectMapper = new ObjectMapper();

        RsEvent original = existingEvents.get(index);
        RsEvent updated = new RsEvent(original);

        long id = original.getId();

        updated.setEventName(null);
        updated.setKeyword("新关键词");

        String serializedUpdated = objectMapper.writeValueAsString(updated);
        performUpdateAndValidate(id, serializedUpdated);

        String serializedOriginal = objectMapper.writeValueAsString(original);
        performUpdateAndValidate(id, serializedOriginal);
    }

    @Test
    void should_update_one_rs_event_when_supplying_keyword_given_id() throws Exception {

        int index = 1;

        ObjectMapper objectMapper = new ObjectMapper();

        RsEvent original = existingEvents.get(index);
        RsEvent updated = new RsEvent(original);

        long id = original.getId();

        updated.setEventName("新热搜");
        updated.setKeyword(null);

        String serializedUpdated = objectMapper.writeValueAsString(updated);
        performUpdateAndValidate(id, serializedUpdated);

        String serializedOriginal = objectMapper.writeValueAsString(original);
        performUpdateAndValidate(id, serializedOriginal);
    }


    @Test
    void should_update_one_rs_event_when_supplying_both_fields_given_id() throws Exception {

        int index = 0;

        ObjectMapper objectMapper = new ObjectMapper();

        RsEvent original = existingEvents.get(index);
        RsEvent updated = new RsEvent(original);

        long id = original.getId();

        updated.setEventName("新关键词热搜");
        updated.setKeyword("新关键词");

        String serializedUpdated = objectMapper.writeValueAsString(updated);
        performUpdateAndValidate(id, serializedUpdated);

        String serializedOriginal = objectMapper.writeValueAsString(original);
        performUpdateAndValidate(id, serializedOriginal);
    }

    private void performUpdateAndValidate(long id, String serializedUpdated) throws Exception {
        mockMvc.perform(patch(ROOT_URL + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedUpdated))

                .andExpect(status().isOk());
    }

    @Test
    void should_get_invalid_param_error_when_update_given_null_user_id() throws Exception {

        int index = 0;

        RsEvent updated = new RsEvent(existingEvents.get(index));
        updated.setUserId(null);
        String serializedUpdated = new ObjectMapper().writeValueAsString(updated);

        mockMvc.perform(patch(ROOT_URL + "/" + updated.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedUpdated))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("invalid param")));
    }

    @Test
    void should_get_bad_request_when_update_given_tampered_user_id() throws Exception {

        int index = 0;

        RsEvent updated = new RsEvent(existingEvents.get(index));
        updated.setUserId(updated.getUserId() + 1);
        String serializedUpdated = new ObjectMapper().writeValueAsString(updated);

        mockMvc.perform(patch(ROOT_URL + "/" + updated.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedUpdated))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("user id incorrect")));
    }

    @Test
    void should_get_not_found_when_update_given_non_existent_id() throws Exception {

        int index = 0;
        long id = 99999999L;

        RsEvent updated = new RsEvent(existingEvents.get(index));
        updated.setId(id);
        String serializedUpdated = new ObjectMapper().writeValueAsString(updated);

        mockMvc.perform(patch(ROOT_URL  + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedUpdated))

                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_entity_id_not_match_requested_resource_id_error_when_update_given_non_existent_id() throws Exception {

        int index = 0;

        RsEvent original = existingEvents.get(index);
        RsEvent updated = new RsEvent(original);

        updated.setId(original.getId() + 1);
        String serializedUpdated = new ObjectMapper().writeValueAsString(updated);

        mockMvc.perform(patch(ROOT_URL  + "/" + original.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedUpdated))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("updated entity id does not match the resource id of the request")));
    }

}
