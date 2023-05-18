package gd.gov.demo.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class JsonResult<T> implements Serializable {
    private int code;
    private T data;
    private String message = "";

    public JsonResult(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public JsonResult(T data) {
        this(200, data, null);
    }

    public JsonResult(int code, String msg) {
        this(code, null, msg);
    }

    public JsonResult(String error) {
        this(400, null, error);
    }

    public JsonResult(Throwable e){
        this(400, null, e.getMessage());
    }
}
