package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.Err;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @PostMapping
    public ResponseEntity add(@RequestBody @Valid User user) {

        userList.add(user);

        return ResponseEntity.created(null)
                .header("index", Integer.toString(userList.indexOf(user)))
                .build();
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    private ResponseEntity<Err> handleUserException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(new Err("invalid user"));
    }

}
