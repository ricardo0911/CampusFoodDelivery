package com.campus.ordering.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
    private String userType; // user, merchant, admin
}
