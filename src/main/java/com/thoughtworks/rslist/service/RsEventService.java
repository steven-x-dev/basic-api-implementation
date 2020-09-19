package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.exception.ResourceExistsException;
import com.thoughtworks.rslist.exception.ResourceNotExistsException;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RsEventService {

    private final RsEventRepository rsEventRepository;

    private final UserRepository userRepository;

    @Autowired
    public RsEventService(RsEventRepository rsEventRepository, UserRepository userRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
    }

    public List<RsEvent> list() {
        List<RsEventPO> rsEventPOs = rsEventRepository.findAll();
        List<RsEvent> events = new ArrayList<>();
        rsEventPOs.forEach(rsEventPO -> events.add(new RsEvent(rsEventPO)));
        return events;
    }

    public RsEvent findById(int id) {
        RsEventPO rsEventPO = rsEventRepository.findById(id);
        if (rsEventPO == null) {
            return null;
        } else {
            return new RsEvent(rsEventPO);
        }
    }

    public RsEvent findByEventName(String eventName) {
        RsEventPO rsEventPO = rsEventRepository.findByEventName(eventName);
        if (rsEventPO == null) {
            return null;
        } else {
            return new RsEvent(rsEventPO);
        }
    }

    public RsEvent findByIdAndEventName(int id, String eventName) {
        RsEventPO rsEventPO = rsEventRepository.findByIdAndEventName(id, eventName);
        if (rsEventPO == null) {
            return null;
        } else {
            return new RsEvent(rsEventPO);
        }
    }

    public int create(RsEvent event) {

        if (!userRepository.existsById(event.getUserId()))
            throw new RsEventNotValidException("user does not exist");

        if (rsEventRepository.existsByEventName(event.getEventName()))
            throw new ResourceExistsException("event name", event.getEventName());

        int userId = event.getUserId();
        UserPO userPO = userRepository.findById(userId);

        RsEventPO rsEventPO = RsEventPO.builder()
                .eventName(event.getEventName())
                .keyword(event.getKeyword())
                .userPO(userPO)
                .build();

        rsEventRepository.save(rsEventPO);

        return rsEventPO.getId();
    }

    public void update(int id, RsEvent updated) {

        if (!rsEventRepository.existsById(id))
            throw new ResourceNotExistsException("event id");

        RsEventPO existing = rsEventRepository.findById(id);

        existing.setEventName(updated.getEventName());
        existing.setKeyword(updated.getKeyword());

        rsEventRepository.save(existing);
    }

    public void deleteById(int id) {
        if (!rsEventRepository.existsById(id)) {
            throw new ResourceNotExistsException("event id");
        }
        rsEventRepository.deleteById(id);
    }

    public void deleteByEventName(String eventName) {
        if (!rsEventRepository.existsByEventName(eventName)) {
            throw new ResourceNotExistsException("event name", eventName);
        }
        rsEventRepository.deleteByEventName(eventName);
    }

    public void deleteByIdAndEventName(int id, String eventName) {
        if (!rsEventRepository.existsByIdAndEventName(id, eventName)) {
            throw new ResourceNotExistsException("event with id and name", eventName);
        }
        rsEventRepository.deleteByIdAndEventName(id, eventName);
    }

}
