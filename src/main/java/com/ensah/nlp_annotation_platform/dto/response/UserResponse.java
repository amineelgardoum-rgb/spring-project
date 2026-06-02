package com.ensah.nlp_annotation_platform.dto.response;

import com.ensah.nlp_annotation_platform.domain.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private List<Role> roles;
    private Boolean enabled;
}
