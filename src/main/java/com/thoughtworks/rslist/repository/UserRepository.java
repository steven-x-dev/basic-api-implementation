package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.UserPO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<UserPO, Integer> {

    boolean existsByUsername(String username);

    boolean existsByIdAndUsername(int id, String username);

    UserPO findById(int id);

    UserPO findByUsername(String username);

    UserPO findByIdAndUsername(int id, String username);

    void deleteByUsername(String username);

    void deleteByIdAndUsername(int id, String username);

    List<UserPO> findAll();
}
