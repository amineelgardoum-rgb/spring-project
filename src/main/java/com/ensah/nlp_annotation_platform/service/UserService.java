package com.ensah.nlp_annotation_platform.service;

import com.ensah.nlp_annotation_platform.dto.UserDto;

public interface UserService {
    UserDto findByUsername(String username);
}
