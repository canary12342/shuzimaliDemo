package com.shuzimali.permission.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName roles
 */
@TableName(value ="roles")
@Data
public class Roles implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer roleId;

    /**
     * 
     */
    private String roleCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}