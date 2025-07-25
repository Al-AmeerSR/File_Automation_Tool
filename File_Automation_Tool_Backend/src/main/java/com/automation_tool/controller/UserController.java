package com.automation_tool.controller;

import com.automation_tool.dto.LoginRequestDTO;
import com.automation_tool.dto.RefreshAndAccessTokenDTO;
import com.automation_tool.dto.UserDTO;
import com.automation_tool.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {

        return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<RefreshAndAccessTokenDTO> loginUser(@RequestBody LoginRequestDTO loginRequestDTO) {

        return new ResponseEntity<>(userService.loginUser(loginRequestDTO),HttpStatus.OK);

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestBody RefreshAndAccessTokenDTO refreshAndAccessTokenDTO) {

        return new ResponseEntity<>(userService.logoutUser(refreshAndAccessTokenDTO),HttpStatus.OK);

    }

    @PostMapping("/refresh")
    public ResponseEntity<HashMap<String,String>> generateAccessToken(@RequestBody RefreshAndAccessTokenDTO refreshAndAccessTokenDTO) {
        return new ResponseEntity<>(userService.generateAccessToken(refreshAndAccessTokenDTO.refreshToken()),HttpStatus.OK);
    }


}
