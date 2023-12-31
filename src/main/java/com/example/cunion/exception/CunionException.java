package com.example.cunion.exception;

import lombok.Data;

@Data
public class CunionException extends RuntimeException{
    private String msg;
    private int code = 500;

    public CunionException(String msg){
        super(msg);
        this.msg = msg;
    }
    public CunionException(String msg, Throwable e){
        super(msg, e);
        this.msg = msg;
    }
    public CunionException(String msg, int code){
        super(msg);
        this.msg = msg;
        this.code = code;
    }
    public CunionException(String msg, int code, Throwable e){
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}
