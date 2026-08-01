package com.smartenergy.common;

import lombok.Data;

/**
 * 统一返回结果
 *
 * @param <T> 数据类型
 * @author smart-energy
 */
@Data
public class Result<T> {

    private int code;

    private String message;

    private T data;

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = null;
        return result;
    }
}
