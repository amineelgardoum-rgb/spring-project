package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.dto.request.admin.CreateUserRequest;
import com.ensah.nlp_annotation_platform.dto.request.admin.UpdateUserRequest;
import com.ensah.nlp_annotation_platform.dto.response.admin.CreatedUserResponse;
import com.ensah.nlp_annotation_platform.dto.response.admin.UserAdminResponse;
import com.ensah.nlp_annotation_platform.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserAdminResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.listUsers(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreatedUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
