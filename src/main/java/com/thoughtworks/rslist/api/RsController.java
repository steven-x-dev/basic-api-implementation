package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/rs")
public class RsController {

    private User alice = new User("Alice", 20, "female", "alice@tw.com", "13000000000");
    private User bob = new User("Bob", 22, "male", "bob@tw.com", "15111111111");
    private User charlie = new User("Charlie", 25, "female", "charlie@tw.com", "18222222222");

    private List<RsEvent> rsList = new ArrayList<RsEvent>() {{
        add(new RsEvent("第一条事件", "政治", alice));
        add(new RsEvent("第二条事件", "经济", bob));
        add(new RsEvent("第三条事件", "文化", charlie));
    }};

    @GetMapping(path = "/{index}")
    public ResponseEntity<RsEvent> find(@PathVariable int index) {
        if (index < 1 || index > rsList.size()) {
            throw new RsEventNotValidException("invalid index");
        }
        return ResponseEntity.ok(rsList.get(index - 1));
    }

    @GetMapping(path = "/list")
    public ResponseEntity<List<RsEvent>> list(@RequestParam(required = false) Integer start,
                                              @RequestParam(required = false) Integer end) {

        if (start == null || end == null) {
            return ResponseEntity.ok(rsList);
        } else {
            if (start < 1 || end > rsList.size() || end < start) {
                throw new RsEventNotValidException("invalid index");
            }
            return ResponseEntity.ok(rsList.subList(start - 1, end));
        }
    }

    @PostMapping
    public ResponseEntity add(@RequestBody @Valid RsEvent event) {

        if (event.getUser() == null) {
            throw new RsEventNotValidException("invalid param");
        }

        rsList.add(event);

        return ResponseEntity.created(null)
                .header("index", Integer.toString(rsList.indexOf(event)))
                .build();
    }

    @PatchMapping(path = "/{index}")
    public ResponseEntity<RsEvent> update(@PathVariable int index, @RequestBody @Valid RsEvent requested) {

        if (index < 1 || index > rsList.size()) {
            throw new RsEventNotValidException("invalid index");
        }

        RsEvent updated = rsList.get(index - 1);

        if (!updated.getEventName().equals(requested.getEventName()))
            updated.setEventName(requested.getEventName());

        if (!updated.getKeyword().equals(requested.getKeyword()))
            updated.setKeyword(requested.getKeyword());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(path = "/{index}")
    public ResponseEntity<RsEvent> delete(@PathVariable int index) {

        if (index < 1 || index > rsList.size()) {
            throw new RsEventNotValidException("invalid index");
        }

        return ResponseEntity.ok(rsList.remove(index));
    }

}
