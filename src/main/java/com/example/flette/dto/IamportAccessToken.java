package com.example.flette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IamportAccessToken {
    private String access_token;
    private long now;
    private long expired_at;
}
