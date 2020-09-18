package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.UserNotValidException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<User> find(@RequestParam(required = false) String username,
                                        @RequestParam(required = false) Integer id) {

        if (username == null && id == null)
            throw new UserNotValidException("missing parameter");

        UserPO userPO;

        if (username == null) {
            int userId = id;
            userPO = userRepository.findById(userId);
        } else if (id == null) {
            userPO = userRepository.findByUsername(username);
        } else {
            int userId = id;
            userPO = userRepository.findByIdAndUsername(userId, username);
        }

        if (userPO != null) {
            return ResponseEntity.ok(new User(userPO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity add(@RequestBody @Validated User newUser) {

        if (userRepository.existsByUsername(newUser.getUsername()))
            throw new UserNotValidException(String.format("username %s is used", newUser.getUsername()));

        UserPO newUserPO = new UserPO(newUser);
        userRepository.save(newUserPO);

        return ResponseEntity.created(null)
                .header("id", Integer.toString(newUserPO.getId()))
                .build();
    }

    @DeleteMapping
    public ResponseEntity deleteById(@RequestParam int id) {
        UserPO existing = userRepository.findById(id);
        if (existing != null) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
