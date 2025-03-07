package com.corefit.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralResponse<T> {
    private String message;
    private T data;

    public GeneralResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public GeneralResponse(String message) {
        this.message = message;
    }

    public GeneralResponse(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
