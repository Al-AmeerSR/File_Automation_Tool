package com.automation_tool.controller;

import com.automation_tool.dto.LoginRequestDTO;
import com.automation_tool.dto.UserDTO;
import com.automation_tool.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {

        return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequestDTO loginRequestDTO) {

        return new ResponseEntity<>(userService.loginUser(loginRequestDTO),HttpStatus.OK);

    }


}
