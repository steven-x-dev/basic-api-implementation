package com.thoughtworks.rslist.po;

import com.thoughtworks.rslist.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPO {

    @Id
    @GeneratedValue
    private int id;

    private String username;

    private String gender;

    private int age;

    private String email;

    private String phone;

    private int votes;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "userPO")
    private List<RsEventPO> rsEventPOs;

    public UserPO(User user) {
        username = user.getUsername();
        gender = user.getGender();
        age = user.getAge();
        email = user.getEmail();
        phone = user.getPhone();
        votes = 10;
    }

}
