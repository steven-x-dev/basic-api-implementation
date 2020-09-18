package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.*;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {

    private static final String ROOT_URL = "/rs";

    @Autowired
    private MockMvc mockMvc;

    private final RsEventRepository rsEventRepository;

    private final UserRepository userRepository;

    @Autowired
    public RsControllerTest(RsEventRepository rsEventRepository, UserRepository userRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
    }

    private User alice;
    private User dave;
    private List<RsEvent> initialRsEvents;

    @BeforeEach
    void setUp() {
        rsEventRepository.deleteAll();
        alice = new User("Alice", 20, "female", "alice@tw.com", "13000000000");
        dave = new User("Dave", 28, "male", "dave@tw.com", "19333333333");
        int userId = userRepository.findByUsername(alice.getUsername()).getId();
        initialRsEvents = new ArrayList<RsEvent>() {{
            add(new RsEvent("第一条事件", "政治", userId));
            add(new RsEvent("第二条事件", "经济", userId));
            add(new RsEvent("第三条事件", "文化", userId));
        }};
        initialRsEvents.forEach(event -> {
            Exception exception = null;
            try {
                should_add_rs_event_and_get_returned_id(event);
            } catch (Exception e) {
                exception = e;
            }
            assertNull(exception);
        });
    }

    @Test
    void should_find_one_rs_event_by_event_name() throws Exception {
        for (RsEvent initialRsEvent : initialRsEvents)
            should_find_one_rs_event_by_event_name(initialRsEvent);
    }

    private void should_find_one_rs_event_by_event_name(RsEvent event) throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get(ROOT_URL).param("eventName", event.getEventName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        validateSingleRsEventResult(resultActions, event);
    }

    void validateSingleRsEventResult(ResultActions resultActions, RsEvent event) throws Exception {
        resultActions
                .andExpect(jsonPath("$.eventName", is(event.getEventName())))
                .andExpect(jsonPath("$.keyword"  , is(event.getKeyword())))
                .andExpect(jsonPath("$.userId"   , is(event.getUserId())));
    }

    void validateListRsEventResult(ResultActions resultActions, List<RsEvent> events) throws Exception {
        for (int i = 0; i < events.size(); i++) {
            RsEvent event = events.get(i);
            resultActions
                    .andExpect(jsonPath(String.format("$[%d].eventName", i), is(event.getEventName())))
                    .andExpect(jsonPath(String.format("$[%d].keyword"  , i), is(event.getKeyword())))
                    .andExpect(jsonPath(String.format("$[%d].userId"   , i), is(event.getUserId())));
        }
    }

    @Test
    void should_get_invalid_index_error_when_find_one_rs_event_given_non_existent_id() throws Exception {
        int id = 99999999;
        mockMvc.perform(
                get(ROOT_URL).param("id", Integer.toString(id)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_list_all_rs_events() throws Exception {

        ResultActions resultActions = mockMvc.perform(get(ROOT_URL + "/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(initialRsEvents.size())));

        validateListRsEventResult(resultActions, initialRsEvents);
    }

    @Test
    void should_add_rs_event_and_get_returned_id() throws Exception {

        int userId = userRepository.findByUsername(alice.getUsername()).getId();
        RsEvent added = new RsEvent("第四条事件", "娱乐", userId);

        should_add_rs_event_and_get_returned_id(added);
    }

    private void should_add_rs_event_and_get_returned_id(RsEvent added) throws Exception {

        String serialized = new ObjectMapper().writeValueAsString(added);

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isCreated())
                .andExpect(header().string("id", any(String.class)));
    }

    @Test
    void should_get_invalid_param_error_when_add_event_given_wrong_param() throws Exception {

        int userId = userRepository.findByUsername(alice.getUsername()).getId();
        RsEvent added = new RsEvent("第四条事件", null, userId);

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
    void should_get_event_name_exists_error_when_add_event_given_existing_event_name() throws Exception {

        int userId = userRepository.findByUsername(alice.getUsername()).getId();
        RsEvent added = new RsEvent("第三条事件", "娱乐", userId);

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
    void should_get_user_not_exists_error_when_add_event_given_non_existent_user_id() throws Exception {

        RsEvent added = new RsEvent("第四条事件", "娱乐", 99999999);
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

    @Test
    void should_update_one_rs_event_when_supplying_one_or_more_field_given_id() throws Exception {

        int index1 = 2;

        RsEvent keywordUpdated = initialRsEvents.get(index1);
        int id1 = rsEventRepository.findByEventName(keywordUpdated.getEventName()).getId();
        keywordUpdated.setKeyword("时事");
        String serializedKeywordUpdated = new ObjectMapper().writeValueAsString(keywordUpdated);

        ResultActions resultActions1 = mockMvc.perform(patch(ROOT_URL)
                .param("id", Integer.toString(id1))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedKeywordUpdated))

                .andExpect(status().isOk());

        validateSingleRsEventResult(resultActions1, keywordUpdated);

        int index2 = 1;

        RsEvent nameUpdated = initialRsEvents.get(index2);
        int id2 = rsEventRepository.findByEventName(nameUpdated.getEventName()).getId();
        keywordUpdated.setEventName("第二条热搜");
        String serializedNameUpdated = new ObjectMapper().writeValueAsString(nameUpdated);

        ResultActions resultActions2 = mockMvc.perform(patch(ROOT_URL)
                .param("id", Integer.toString(id2))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedNameUpdated))

                .andExpect(status().isOk());

        validateSingleRsEventResult(resultActions2, nameUpdated);

        int index3 = 0;

        RsEvent bothUpdated = initialRsEvents.get(index3);
        int id3 = rsEventRepository.findByEventName(bothUpdated.getEventName()).getId();
        bothUpdated.setEventName("第一条热搜");
        bothUpdated.setKeyword("历史");
        String serializedBothUpdated = new ObjectMapper().writeValueAsString(bothUpdated);

        ResultActions resultActions3 = mockMvc.perform(patch(ROOT_URL)
                .param("id", Integer.toString(id3))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedBothUpdated))

                .andExpect(status().isOk());

        validateSingleRsEventResult(resultActions3, bothUpdated);
    }

    @Test
    void should_get_invalid_param_error_when_update_given_wrong_param() throws Exception {

        int index = 0;

        RsEvent keywordUpdated = initialRsEvents.get(index);
        int id = rsEventRepository.findByEventName(keywordUpdated.getEventName()).getId();
        keywordUpdated.setKeyword(null);
        String serializedKeywordUpdated = new ObjectMapper().writeValueAsString(keywordUpdated);

        mockMvc.perform(patch(ROOT_URL)
                .param("id", Integer.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedKeywordUpdated))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("invalid param")));
    }

    @Test
    void should_get_not_found_when_update_given_non_existent_id() throws Exception {

        int index = 0;
        int id = 99999999;

        RsEvent updated = initialRsEvents.get(index);
        String serializedKeywordUpdated = new ObjectMapper().writeValueAsString(updated);

        mockMvc.perform(patch(ROOT_URL)
                .param("id", Integer.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedKeywordUpdated))

                .andExpect(status().isNotFound());
    }

    @Test
    void should_delete_given_index() throws Exception {

        int index = 1;

        RsEvent deleted = initialRsEvents.get(index);
        int id = rsEventRepository.findByEventName(deleted.getEventName()).getId();

        long countBefore = rsEventRepository.count();

        mockMvc.perform(delete(ROOT_URL)
                .param("id", Integer.toString(id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isOk());

        assertEquals(countBefore - 1, rsEventRepository.count());
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
