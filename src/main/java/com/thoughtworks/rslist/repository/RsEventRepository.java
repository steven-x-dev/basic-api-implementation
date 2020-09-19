package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.RsEventPO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventPO, Integer> {

    boolean existsByEventName(String eventName);

    boolean existsByIdAndEventName(int id, String eventName);

    RsEventPO findById(int id);

    RsEventPO findByEventName(String eventName);

    RsEventPO findByIdAndEventName(int id, String eventName);

    void deleteByEventName(String eventName);

    void deleteByIdAndEventName(int id, String eventName);

    List<RsEventPO> findAll();

}
