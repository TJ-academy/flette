package com.example.flette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IamportResponse<T> {
    private int code;
    private String message;
    private T response;
}
