package com.ensah.nlp_annotation_platform.service;

import com.ensah.nlp_annotation_platform.dto.response.UserResponse;

public interface UserService {
    UserResponse findByUsername(String username);
}
