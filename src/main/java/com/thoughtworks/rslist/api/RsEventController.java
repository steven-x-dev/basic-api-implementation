package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.exception.ParameterMissingException;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.service.RsEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/rs")
public class RsEventController {

    private final RsEventService rsEventService;

    @Autowired
    public RsEventController(RsEventService rsEventService) {
        this.rsEventService = rsEventService;
    }

    @GetMapping(path = "/list")
    public ResponseEntity<List<RsEvent>> list() {
        List<RsEvent> events = rsEventService.list();
        return ResponseEntity.ok(events);
    }

    @GetMapping
    public ResponseEntity<RsEvent> find(@RequestParam(required = false) String eventName,
                                        @RequestParam(required = false) Long id) {

        if (eventName == null && id == null)
            throw new ParameterMissingException("eventName", "id");

        RsEvent event;

        if (eventName == null) {
            event = rsEventService.findById(id);
        } else if (id == null) {
            event = rsEventService.findByEventName(eventName);
        } else {
            event = rsEventService.findByIdAndEventName(id, eventName);
        }

        if (event != null) {
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity create(@RequestBody @Validated RsEvent event) {

        if (event.getEventName() == null || event.getKeyword() == null)
            throw new RsEventNotValidException("invalid param");

        long newEventId = rsEventService.create(event);

        return ResponseEntity.created(null)
                .header("id", Long.toString(newEventId))
                .build();
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity update(@PathVariable long id,
                                 @RequestBody @Validated RsEvent updated) {

        if (updated.getId() == null || updated.getId() != id) {
            throw new RsEventNotValidException("updated entity id does not match the resource id of the request");
        }

        rsEventService.update(id, updated);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity delete(@RequestParam(required = false) String eventName,
                                 @RequestParam(required = false) Long id) {

        if (eventName == null && id == null)
            throw new ParameterMissingException("event name", "id");

        if (eventName == null) {
            rsEventService.deleteById(id);
        } else if (id == null) {
            rsEventService.deleteByEventName(eventName);
        } else {
            rsEventService.deleteByIdAndEventName(id, eventName);
        }

        return ResponseEntity.ok().build();
    }

}
