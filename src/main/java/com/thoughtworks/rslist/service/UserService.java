package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.ResourceExistsException;
import com.thoughtworks.rslist.exception.ResourceNotExistsException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<User> list() {
        List<UserPO> userPOs = userRepository.findAll();
        List<User> users = new ArrayList<>();
        userPOs.forEach(userPO -> users.add(new User(userPO)));
        return users;
    }

    public User findById(int id) {
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

    public User findByIdAndUsername(int id, String username) {
        UserPO userPO = userRepository.findByIdAndUsername(id, username);
        if (userPO == null) {
            return null;
        } else {
            return new User(userPO);
        }
    }

    public boolean existsById(int id) {
        return userRepository.existsById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public int create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResourceExistsException("username", user.getUsername());
        }
        UserPO userPO = new UserPO(user);
        userRepository.save(userPO);
        return userPO.getId();
    }

    public void deleteById(int id) {
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

    public void deleteByIdAndUsername(int id, String username) {
        if (!userRepository.existsByIdAndUsername(id, username)) {
            throw new ResourceNotExistsException("user with id and username", username);
        }
        userRepository.deleteByIdAndUsername(id, username);
    }

}
