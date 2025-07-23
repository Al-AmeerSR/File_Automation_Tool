package com.automation_tool.dto;

import com.automation_tool.entity.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Role role;
}
