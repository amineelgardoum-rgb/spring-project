package com.ensah.nlp_annotation_platform.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String roles;
    private Boolean enabled;
}
