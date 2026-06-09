package com.ensah.nlp_annotation_platform.service.admin;

import com.ensah.nlp_annotation_platform.dto.request.admin.CreateUserRequest;
import com.ensah.nlp_annotation_platform.dto.request.admin.UpdateUserRequest;
import com.ensah.nlp_annotation_platform.dto.response.admin.CreatedUserResponse;
import com.ensah.nlp_annotation_platform.dto.response.admin.UserAdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    Page<UserAdminResponse> listUsers(Pageable pageable);
    CreatedUserResponse createUser(CreateUserRequest request);
    UserAdminResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}
