package com.shuzimali.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName transaction_logs
 */
@TableName(value ="transaction_logs")
@Data
public class TransactionLogs implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    private String transactionId;

    private int status;

    /**
     * 
     */
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}