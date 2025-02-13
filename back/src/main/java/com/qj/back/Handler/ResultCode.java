package com.qj.back.Handler;

public enum ResultCode {
    SUCCESS(200,"登录成功"),

    ERROR(-100,"登陆失败");

    private Integer code;
    private String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
