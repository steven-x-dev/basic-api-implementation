package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/rs")
public class RsController {

  private List<RsEvent> rsList = new ArrayList<RsEvent>() {{
    add(new RsEvent("第一条事件", "政治"));
    add(new RsEvent("第二条事件", "经济"));
    add(new RsEvent("第三条事件", "文化"));
  }};

  @GetMapping(path = "/{index}")
  public RsEvent find(@PathVariable int index) {
    return rsList.get(index - 1);
  }

  @GetMapping
  public List<RsEvent> list(@RequestParam(required = false) Integer start,
                            @RequestParam(required = false) Integer end) {

    if (start == null || end == null) {
      return new ArrayList<>(rsList);
    } else {
      return new ArrayList<>(rsList.subList(start - 1, end));
    }
  }

  @PostMapping
  public RsEvent add(@RequestBody String request) throws JsonProcessingException {
    RsEvent event = new ObjectMapper().readValue(request, RsEvent.class);
    rsList.add(event);
    return event;
  }

  @PatchMapping(path = "/{index}")
  public RsEvent update(@PathVariable int index, @RequestBody String request) throws JsonProcessingException {

    RsEvent requested = new ObjectMapper().readValue(request, RsEvent.class);

    RsEvent updated = rsList.get(index - 1);

    if (!updated.getEventName().equals(requested.getEventName()))
      updated.setEventName(requested.getEventName());

    if (!updated.getKeyword().equals(requested.getKeyword()))
      updated.setKeyword(requested.getKeyword());

    return updated;
  }

  @DeleteMapping(path = "/{index}")
  public RsEvent delete(@PathVariable int index) {
    return rsList.remove(index);
  }

}
