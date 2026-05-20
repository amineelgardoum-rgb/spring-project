package com.ensah.nlp_annotation_platform.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String roles;
    private Boolean enabled;
}
