package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.api.RsController;
import com.thoughtworks.rslist.domain.RsEvent;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
class RsControllerTest {

    private static final String ROOT_URL = "/rs";

    private MockMvc mockMvc;

    private User alice;
    private List<RsEvent> clientData;

    @BeforeEach
    void init() {
        clientData = new ArrayList<RsEvent>() {{
            add(new RsEvent("第一条事件", "政治"));
            add(new RsEvent("第二条事件", "经济"));
            add(new RsEvent("第三条事件", "文化"));
        }};
        alice = new User("Alice", 20, "female", "alice@tw.com", "13000000000");
        mockMvc = MockMvcBuilders.standaloneSetup(new RsController()).build();
    }

    @Test
    void should_find_one_rs_event_by_index() throws Exception {
        for (int i = 1; i <= clientData.size(); i++)
            should_find_one_rs_event_by_index(i);
    }

    private void should_find_one_rs_event_by_index(int index) throws Exception {
        mockMvc.perform(get(ROOT_URL + "/" + index))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(clientData.get(index - 1).getEventName())))
                .andExpect(jsonPath("$.keyword", is(clientData.get(index - 1).getKeyword())))
                .andExpect(jsonPath("$", not(hasKey("user"))));
    }

    @Test
    void should_list_all_rs_events() throws Exception {

        String serializedExpectedResult =
                new ObjectMapper().writeValueAsString(clientData);

        mockMvc.perform(get(ROOT_URL + "/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(result -> assertEquals(serializedExpectedResult,
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8)));
    }

    @Test
    void should_list_several_rs_events_given_start_and_end() throws Exception {

        int start = 2;
        int end = 3;

        String serializedExpectedResult = new ObjectMapper().writeValueAsString(clientData.subList(start - 1, end));

        mockMvc.perform(get(ROOT_URL + "/list")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .param("start", Integer.toString(start))
                .param("end", Integer.toString(end)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(end - start + 1)))
                .andExpect(result -> assertEquals(serializedExpectedResult,
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8)));
    }

    @Test
    void should_add_rs_event_and_get_returned_added_event_given_event_data_as_string() throws Exception {

        RsEvent added = new RsEvent("第四条事件", "娱乐", alice);

        ObjectMapper objectMapper = new ObjectMapper();

        String userString = objectMapper.writeValueAsString(alice);
        String eventString = objectMapper.writeValueAsString(added);
        String serialized = eventString.substring(0, eventString.length() - 1) + ",\"user\":" + userString + "}";

        mockMvc.perform(post(ROOT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isCreated())
                .andExpect(header().string("index", any(String.class)));
    }

    @Test
    void should_update_one_rs_event_when_supplying_one_or_more_field_given_id() throws Exception {

        int index1 = 3;

        RsEvent keywordUpdated = clientData.get(index1 - 1);
        keywordUpdated.setKeyword("时事");
        String serializedKeywordUpdated = new ObjectMapper().writeValueAsString(keywordUpdated);

        mockMvc.perform(patch(ROOT_URL + "/" + index1)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedKeywordUpdated))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(keywordUpdated.getEventName())))
                .andExpect(jsonPath("$.keyword", is(keywordUpdated.getKeyword())));

        int index2 = 2;

        RsEvent nameUpdated = clientData.get(index2 - 1);
        keywordUpdated.setEventName("第二条热搜");
        String serializedNameUpdated = new ObjectMapper().writeValueAsString(nameUpdated);

        mockMvc.perform(patch(ROOT_URL + "/" + index2)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedNameUpdated))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(nameUpdated.getEventName())))
                .andExpect(jsonPath("$.keyword", is(nameUpdated.getKeyword())));

        int index3 = 1;

        RsEvent bothUpdated = clientData.get(index3 - 1);
        bothUpdated.setEventName("第一条热搜");
        bothUpdated.setKeyword("历史");
        String serializedBothUpdated = new ObjectMapper().writeValueAsString(bothUpdated);

        mockMvc.perform(patch(ROOT_URL + "/" + index3)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serializedBothUpdated))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(bothUpdated.getEventName())))
                .andExpect(jsonPath("$.keyword", is(bothUpdated.getKeyword())));
    }

    @Test
    void should_delete_given_index() throws Exception {

        int index = 1;
        RsEvent deleted = clientData.get(index);

        mockMvc.perform(delete(ROOT_URL + "/" + index)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(deleted.getEventName())))
                .andExpect(jsonPath("$.keyword", is(deleted.getKeyword())));
    }

}
