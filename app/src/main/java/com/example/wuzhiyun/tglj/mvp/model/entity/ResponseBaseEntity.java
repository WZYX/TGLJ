package com.example.wuzhiyun.tglj.mvp.model.entity;


public class ResponseBaseEntity<T> {
    private int error;
    private String msg;
    private T Data;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }
}
