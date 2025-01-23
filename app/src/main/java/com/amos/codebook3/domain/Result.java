package com.amos.codebook3.domain;

//返回结果类
public class Result<T> {
    public String message;
    public T data;

    public Result(String message, T data) {
        this.message = message;
        this.data = data;
    }
}
