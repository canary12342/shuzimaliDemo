package com.shuzimali.entity;

import com.shuzimali.utils.ErrorCode;
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
