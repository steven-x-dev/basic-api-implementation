package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.UserPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends PagingAndSortingRepository<UserPO, Long> {

    boolean existsByUsername(String username);

    boolean existsByIdAndUsername(long id, String username);

    UserPO findById(long id);

    UserPO findByUsername(String username);

    UserPO findByIdAndUsername(long id, String username);

    @Transactional
    void deleteByUsername(String username);

    @Transactional
    void deleteByIdAndUsername(long id, String username);

    @Override
    Page<UserPO> findAll(Pageable pageable);

}
