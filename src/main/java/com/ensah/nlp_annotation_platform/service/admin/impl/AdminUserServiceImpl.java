package com.ensah.nlp_annotation_platform.service.admin.impl;

import com.ensah.nlp_annotation_platform.domain.Role;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.request.admin.CreateUserRequest;
import com.ensah.nlp_annotation_platform.dto.request.admin.UpdateUserRequest;
import com.ensah.nlp_annotation_platform.dto.response.admin.CreatedUserResponse;
import com.ensah.nlp_annotation_platform.dto.response.admin.UserAdminResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.mapper.UserAdminMapper;
import com.ensah.nlp_annotation_platform.repository.UserRepository;
import com.ensah.nlp_annotation_platform.service.admin.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserAdminMapper userAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminUserServiceImpl(UserRepository userRepository, UserAdminMapper userAdminMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAdminMapper = userAdminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserAdminResponse> listUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable)
                .map(userAdminMapper::toAdminResponse);
    }

    @Override
    public CreatedUserResponse createUser(CreateUserRequest request) {
        String generatedPassword = generatePassword();
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(generatedPassword));
        user.setRoles(List.of(Role.ROLE_ANNOTATOR));
        user.setEnabled(true);
        user.setDeleted(false);
        userRepository.save(user);
        return new CreatedUserResponse(user.getId(), user.getUsername(), generatedPassword);
    }

    @Override
    public UserAdminResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        userRepository.save(user);
        return userAdminMapper.toAdminResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeleted(true);
        userRepository.save(user);
    }

    private String generatePassword() {
        byte[] randomBytes = new byte[12];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
