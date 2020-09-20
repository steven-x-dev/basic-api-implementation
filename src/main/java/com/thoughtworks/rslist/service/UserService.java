package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.ResourceExistsException;
import com.thoughtworks.rslist.exception.ResourceNotExistsException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> list(int pageSize, int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<UserPO> page = userRepository.findAll(pageable);
        List<User> users = new ArrayList<>();
        page.forEach(userPO -> users.add(new User(userPO)));
        return users;
    }

    public User findById(long id) {
        UserPO userPO = userRepository.findById(id);
        if (userPO == null) {
            return null;
        } else {
            return new User(userPO);
        }
    }

    public User findByUsername(String username) {
        UserPO userPO = userRepository.findByUsername(username);
        if (userPO == null) {
            return null;
        } else {
            return new User(userPO);
        }
    }

    public User findByIdAndUsername(long id, String username) {
        UserPO userPO = userRepository.findByIdAndUsername(id, username);
        if (userPO == null) {
            return null;
        } else {
            return new User(userPO);
        }
    }

    public boolean existsById(long id) {
        return userRepository.existsById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public long create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResourceExistsException("username", user.getUsername());
        }
        UserPO userPO = UserPO.builder()
                .username(user.getUsername())
                .gender(user.getGender())
                .age(user.getAge())
                .email(user.getEmail())
                .phone(user.getPhone())
                .votes(10)
                .build();
        userRepository.save(userPO);
        return userPO.getId();
    }

    public void deleteById(long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotExistsException("user id");
        }
        userRepository.deleteById(id);
    }

    public void deleteByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotExistsException("username", username);
        }
        userRepository.deleteByUsername(username);
    }

    public void deleteByIdAndUsername(long id, String username) {
        if (!userRepository.existsByIdAndUsername(id, username)) {
            throw new ResourceNotExistsException("user with id and username", username);
        }
        userRepository.deleteByIdAndUsername(id, username);
    }

}
