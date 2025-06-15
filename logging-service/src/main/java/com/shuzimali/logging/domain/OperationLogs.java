package com.shuzimali.logging.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName operation_logs
 */
@TableName(value ="operation_logs")
@Data
public class OperationLogs implements Serializable {
    /**
     * 
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String action;

    /**
     * 
     */
    private String ip;

    /**
     * 
     */
    private String detail;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}