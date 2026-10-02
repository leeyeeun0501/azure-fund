package io.kickscar.cloud.workload.gallery.dto;

import lombok.Getter;

@Getter
public class JsonResult<T> {
    private final String result;    // "success" or "fail"
    private final T data;           // set if the result is success
    private final String message;   // set if the result is fail

    public static JsonResult<Object> fail(String message) {
        return new JsonResult<>(message);
    }

    public static <T> JsonResult<T> success(T data) {
        return new JsonResult<>(data);
    }

    private JsonResult(String message) {
        result = "fail";
        data = null;
        this.message = message;
    }

    private JsonResult(T data) {
        result = "success";
        this.data = data;
        message = null;
    }
}