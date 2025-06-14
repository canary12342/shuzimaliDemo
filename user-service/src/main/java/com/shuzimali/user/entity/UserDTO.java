package com.shuzimali.user.entity;


import lombok.Data;


@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;
}