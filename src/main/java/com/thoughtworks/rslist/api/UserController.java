package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.UserNameOccupiedException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @GetMapping(path = "/list")
    public ResponseEntity<List<User>> list() {

        List<UserPO> userPOs = userRepository.findAll();

        List<User> users = new ArrayList<>();
        userPOs.forEach(userPO -> users.add(new User(userPO)));

        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<User> findById(@PathVariable int id) {
        UserPO userPO = userRepository.findById(id);
        if (userPO != null) {
            return ResponseEntity.ok(new User(userPO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/{username}")
    public ResponseEntity<User> findByUsername(@PathVariable String username) {
        UserPO userPO = userRepository.findByUsername(username);
        if (userPO != null) {
            return ResponseEntity.ok(new User(userPO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity add(@RequestBody @Valid User newUser) {

        if (userRepository.existsByUsername(newUser.getUsername()))
            throw new UserNameOccupiedException(String.format("username %s is used", newUser.getUsername()));

        UserPO newUserPO = new UserPO(newUser);
        userRepository.save(newUserPO);

        return ResponseEntity.created(null)
                .header("id", Integer.toString(newUserPO.getId()))
                .build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deleteById(@PathVariable int id) {
        UserPO existing = userRepository.findById(id);
        if (existing != null) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
