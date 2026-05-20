package com.ensah.nlp_annotation_platform.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
