package com.shuzimali.user.entity;

import lombok.Data;

@Data
public class PasswordDTO {
    private  String oldPassword;
    private  String newPassword;
}
