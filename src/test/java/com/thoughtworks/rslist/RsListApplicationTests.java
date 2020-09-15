package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.api.RsController;
import com.thoughtworks.rslist.domain.RsEvent;
import org.junit.jupiter.api.AfterEach;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class RsListApplicationTests {

    private static final String ROOT_URL = "/rs";

    @Autowired
    private MockMvc mockMvc;

    private List<RsEvent> initialData;

    @BeforeEach
    void load_data() {
        initialData = new ArrayList<RsEvent>() {{
            add(new RsEvent("第一条事件", "政治"));
            add(new RsEvent("第二条事件", "经济"));
            add(new RsEvent("第三条事件", "文化"));
        }};
    }

    @AfterEach
    void restore_data() throws Exception {
        mockMvc.perform(post(ROOT_URL + "/restore"));
    }

    @Test
    void should_find_one_rs_event_by_index() throws Exception {
        for (int i = 1; i <= initialData.size(); i++)
            should_find_one_rs_event_by_index(i);
    }

    private void should_find_one_rs_event_by_index(int index) throws Exception {
        mockMvc.perform(get(ROOT_URL + "/find?index=" + index))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(initialData.get(index - 1).getEventName())))
                .andExpect(jsonPath("$.keyword", is(initialData.get(index - 1).getKeyword())));
    }

    @Test
    void should_list_all_rs_events() throws Exception {

        String serializedExpectedResult = new ObjectMapper().writeValueAsString(initialData);

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

        String serializedExpectedResult = new ObjectMapper().writeValueAsString(initialData.subList(start - 1, end));

        mockMvc.perform(get(ROOT_URL + String.format("/list?start=%d&end=%d", start, end)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(end - start + 1)))
                .andExpect(result -> assertEquals(serializedExpectedResult,
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8)));
    }

    @Test
    void should_add_rs_event_and_get_returned_added_event_given_event_data_as_string() throws Exception {

        RsEvent added = new RsEvent("第四条事件", "娱乐");
        String serialized = new ObjectMapper().writeValueAsString(added);

        mockMvc.perform(post(ROOT_URL + "/add")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(serialized))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(added.getEventName())))
                .andExpect(jsonPath("$.keyword", is(added.getKeyword())));
    }


    @Test
    void should_update_one_rs_event_when_supplying_one_or_more_field_given_id() throws Exception {

        RsEvent keywordUpdated = new RsEvent("第三条事件", "时事");
        String serializedKeywordUpdated = new ObjectMapper().writeValueAsString(keywordUpdated);

        mockMvc.perform(patch(ROOT_URL + "/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .param("index", Integer.toString(3))
                .content(serializedKeywordUpdated))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(keywordUpdated.getEventName())))
                .andExpect(jsonPath("$.keyword", is(keywordUpdated.getKeyword())));

        RsEvent nameUpdated = new RsEvent("第二条热搜", "经济");
        String serializedNameUpdated = new ObjectMapper().writeValueAsString(nameUpdated);

        mockMvc.perform(patch(ROOT_URL + "/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .param("index", Integer.toString(2))
                .content(serializedNameUpdated))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(nameUpdated.getEventName())))
                .andExpect(jsonPath("$.keyword", is(nameUpdated.getKeyword())));

        RsEvent bothUpdated = new RsEvent("第一条热搜", "历史");
        String serializedBothUpdated = new ObjectMapper().writeValueAsString(bothUpdated);

        mockMvc.perform(patch(ROOT_URL + "/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .param("index", Integer.toString(1))
                .content(serializedBothUpdated))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(bothUpdated.getEventName())))
                .andExpect(jsonPath("$.keyword", is(bothUpdated.getKeyword())));
    }

//    @Test
    void should_delete_given_index() throws Exception {

        int index = 1;
        RsEvent deleted = initialData.get(index);

        mockMvc.perform(delete(ROOT_URL + "/delete")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .param("index", Integer.toString(index)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventName", is(deleted.getEventName())))
                .andExpect(jsonPath("$.keyword", is(deleted.getKeyword())));
    }

}
