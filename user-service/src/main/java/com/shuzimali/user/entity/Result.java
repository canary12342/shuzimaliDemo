package com.shuzimali.user.entity;

import com.shuzimali.user.utils.ErrorCode;
import lombok.Data;

@Data
public class Result<T> {
    private int code;

    private T data;

    private String message;

    public Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Result(ErrorCode errorCode) {
    }
}
