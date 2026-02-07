package com.mybank.bankingapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage<T>{
    private String status;
    private String message;
    private T data;
    private Object error;

    public ResponseMessage(String status, String message, T data, Object error) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseMessage<T> success(String message, T data) {
        return new ResponseMessage<>("success", message, data, null);
    }
    public static <T> ResponseMessage<T> error(String message, Object errors) {
        return new ResponseMessage<>("error", message, null, errors);
    }
}
