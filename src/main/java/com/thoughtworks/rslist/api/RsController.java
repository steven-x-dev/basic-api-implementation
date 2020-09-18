package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/rs")
public class RsController {

    @Autowired
    RsEventRepository rsEventRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    public ResponseEntity<RsEvent> find(@RequestParam(required = false) String eventName,
                                        @RequestParam(required = false) Integer id) {

        if (eventName == null && id == null)
            throw new RsEventNotValidException("missing parameter");

        RsEventPO rsEventPO;

        if (eventName == null) {
            int eventId = id;
            rsEventPO = rsEventRepository.findById(eventId);
        } else if (id == null) {
            rsEventPO = rsEventRepository.findByEventName(eventName);
        } else {
            int eventId = id;
            rsEventPO = rsEventRepository.findByIdAndEventName(eventId, eventName);
        }

        if (rsEventPO != null) {
            return ResponseEntity.ok(new RsEvent(rsEventPO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/list")
    public ResponseEntity<List<RsEvent>> list() {

        List<RsEventPO> rsEventPOs = rsEventRepository.findAll();

        List<RsEvent> events = new ArrayList<>();
        rsEventPOs.forEach(rsEventPO -> events.add(new RsEvent(rsEventPO)));

        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity add(@RequestBody @Validated RsEvent event) {

        if (event.getUserId() == null)
            throw new RsEventNotValidException("invalid param");

        int userId = event.getUserId();
        UserPO userPO = userRepository.findById(userId);

        if (userPO == null)
            throw new RsEventNotValidException("user does not exist");

        if (rsEventRepository.existsByEventName(event.getEventName()))
            throw new RsEventNotValidException(String.format("event name %s already exists", event.getEventName()));

        RsEventPO rsEventPO = RsEventPO.builder()
                .eventName(event.getEventName())
                .keyword(event.getKeyword())
                .userPO(userPO)
                .build();

        rsEventRepository.save(rsEventPO);

        return ResponseEntity.created(null)
                .header("id", Integer.toString(rsEventPO.getId()))
                .build();
    }

    @PatchMapping
    public ResponseEntity<RsEvent> update(@RequestParam int id, @RequestBody @Valid RsEvent requested) {

        RsEventPO existing = rsEventRepository.findById(id);

        if (existing == null)
            return ResponseEntity.notFound().build();

        existing.setEventName(requested.getEventName());
        existing.setKeyword(requested.getKeyword());

        rsEventRepository.save(existing);

        return ResponseEntity.ok(new RsEvent(existing));
    }

    @DeleteMapping
    public ResponseEntity deleteById(@RequestParam int id) {
        RsEventPO existing = rsEventRepository.findById(id);
        if (existing != null) {
            rsEventRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
