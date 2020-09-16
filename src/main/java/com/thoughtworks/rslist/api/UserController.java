package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private List<User> userList = new ArrayList<User>() {{
        add(new User("Alice", 20, "female", "alice@tw.com", "13000000000"));
        add(new User("Bob", 22, "male", "bob@tw.com", "15111111111"));
        add(new User("Charlie", 25, "female", "charlie@tw.com", "18222222222"));
    }};

    @GetMapping(path = "/list")
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(userList);
    }


}
