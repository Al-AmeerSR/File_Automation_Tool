package com.automation_tool.mapper;

import com.automation_tool.dto.UserDTO;
import com.automation_tool.entity.User;

public class UserMapper {

    public static  UserDTO mapToUserDTO(User user) {
        return new UserDTO(user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole());
    }

    public static User mapToUser(UserDTO userDTO) {
       return new User(userDTO.getId(),
               userDTO.getUsername(),
               userDTO.getPassword(),
               userDTO.getEmail(),
               userDTO.getRole());
    }
}
