package com.shuzimali.logging.domain;

import lombok.Data;

@Data
public class Event implements java.io.Serializable{
    private Long messageId;
    private Long userId;
    private String action;
    private String ip;
    private String detail;
}
