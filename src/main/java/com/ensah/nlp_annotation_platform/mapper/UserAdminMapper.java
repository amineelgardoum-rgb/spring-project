package com.ensah.nlp_annotation_platform.mapper;

import com.ensah.nlp_annotation_platform.domain.Role;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.response.admin.UserAdminResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserAdminMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserAdminResponse toAdminResponse(User user);

    @Named("rolesToStrings")
    default List<String> rolesToStrings(List<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Enum::name).collect(Collectors.toList());
    }
}
