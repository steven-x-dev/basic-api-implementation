package com.thoughtworks.rslist.domain;

import com.thoughtworks.rslist.po.UserPO;
import lombok.*;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    @NotNull
    @Size(min = 3, max = 8)
    private String username;

    @NotNull
    @Min(18)
    @Max(100)
    private Integer age;

    @NotNull
    private String gender;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Pattern(regexp = "1\\d{10}")
    private String phone;

    @Min(0)
    private Integer votes;

    public User(@NotNull @Size(min = 3, max = 8) String username,
                @NotNull @Min(18) @Max(100) Integer age,
                @NotNull String gender,
                @NotNull @Email String email,
                @NotNull @Pattern(regexp = "1\\d{10}") String phone) {

        this.username = username;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
    }

    public User(UserPO userPO) {
        id = userPO.getId();
        username = userPO.getUsername();
        age = userPO.getAge();
        gender = userPO.getGender();
        email = userPO.getEmail();
        phone = userPO.getPhone();
        votes = userPO.getVotes();
    }

    public User(User user) {
        id = user.getId();
        username = user.getUsername();
        age = user.getAge();
        gender = user.getGender();
        email = user.getEmail();
        phone = user.getPhone();
        votes = user.getVotes();
    }

}
