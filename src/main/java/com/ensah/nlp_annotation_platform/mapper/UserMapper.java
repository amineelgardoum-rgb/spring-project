package com.ensah.nlp_annotation_platform.mapper;

import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
