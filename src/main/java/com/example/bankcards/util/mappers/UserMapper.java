package com.example.bankcards.util.mappers;

import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.UserModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(UserModel user);
}
