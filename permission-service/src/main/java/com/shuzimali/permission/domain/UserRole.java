package com.shuzimali.permission.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;


@Data
@TableName("user_roles")
public class UserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer roleId;
}