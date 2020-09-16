package com.thoughtworks.rslist.domain;

import javax.validation.constraints.*;

public class User {

    @NotNull
    @Size(min = 4, max = 100)
    private String name;

    @NotNull
    @Min(18)
    @Min(100)
    private Integer age;

    @NotNull
    private String gender;

    @Email
    private String email;

    @Pattern(regexp = "1\\d{10}")
    private String phone;

    @Min(0)
    private int votes = 10;

    public User() {}

    public User(@NotNull @Size(min = 4, max = 100) String name, @NotNull @Min(18) @Min(100) Integer age, @NotNull String gender, @Email String email, @Pattern(regexp = "1\\d{10}") String phone) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
