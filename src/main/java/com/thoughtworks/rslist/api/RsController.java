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

  @GetMapping(path = "/find")
  public RsEvent find(@RequestParam int index) {
    return rsList.get(index - 1);
  }

  @GetMapping(path = "/list")
  public List<RsEvent> list(@RequestParam(required = false) Integer start,
                            @RequestParam(required = false) Integer end) {

    if (start == null || end == null) {
      return new ArrayList<>(rsList);
    } else {
      return new ArrayList<>(rsList.subList(start - 1, end));
    }
  }

}
