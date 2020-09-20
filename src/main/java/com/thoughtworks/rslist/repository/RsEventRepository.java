package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.RsEventPO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventPO, Long> {

    boolean existsByEventName(String eventName);

    boolean existsByIdAndEventName(long id, String eventName);

    RsEventPO findById(long id);

    RsEventPO findByEventName(String eventName);

    RsEventPO findByIdAndEventName(long id, String eventName);

    @Transactional
    void deleteByEventName(String eventName);

    @Transactional
    void deleteByIdAndEventName(long id, String eventName);

    @Override
    List<RsEventPO> findAll();

}
