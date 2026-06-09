package com.ensah.nlp_annotation_platform.dto.response.admin;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class UserAdminResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private List<String> roles;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
