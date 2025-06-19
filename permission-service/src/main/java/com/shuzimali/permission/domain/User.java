package com.shuzimali.permission.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
}