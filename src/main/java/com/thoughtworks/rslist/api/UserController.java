package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.ParameterMissingException;
import com.thoughtworks.rslist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/list")
    public ResponseEntity<List<User>> list() {
        List<User> users = userService.list();
        return ResponseEntity.ok(users);
    }

    @GetMapping
    public ResponseEntity<User> find(@RequestParam(required = false) String username,
                                     @RequestParam(required = false) Integer id) {

        if (username == null && id == null)
            throw new ParameterMissingException("username", "id");

        User user;

        if (username == null) {
            user = userService.findById(id);
        } else if (id == null) {
            user = userService.findByUsername(username);
        } else {
            user = userService.findByIdAndUsername(id, username);
        }

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity register(@RequestBody @Validated User user) {
        int newUserId = userService.create(user);
        return ResponseEntity.created(null)
                .header("id", Integer.toString(newUserId))
                .build();
    }

    @DeleteMapping
    public ResponseEntity delete(@RequestParam(required = false) String username,
                                 @RequestParam(required = false) Integer id) {

        if (username == null && id == null)
            throw new ParameterMissingException("username", "id");

        if (username == null) {
            userService.deleteById(id);
        } else if (id == null) {
            userService.deleteByUsername(username);
        } else {
            userService.deleteByIdAndUsername(id, username);
        }

        return ResponseEntity.ok().build();
    }

}
