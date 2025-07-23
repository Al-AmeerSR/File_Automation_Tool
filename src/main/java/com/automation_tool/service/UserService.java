package com.automation_tool.service;

import com.automation_tool.dto.LoginRequestDTO;
import com.automation_tool.dto.UserDTO;
import com.automation_tool.entity.User;
import com.automation_tool.mapper.UserMapper;
import com.automation_tool.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

   private final  BCryptPasswordEncoder passwordEncoder;
   private final UserRepository userRepository;
   private final AuthenticationManager authenticationManager;

   public UserService(BCryptPasswordEncoder passwordEncoder,
                      UserRepository userRepository,
                      AuthenticationManager authenticationManager) {
       this.passwordEncoder = passwordEncoder;
       this.userRepository = userRepository;
       this.authenticationManager = authenticationManager;
   }

    public String registerUser(UserDTO userDTO){
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User user =UserMapper.mapToUser(userDTO);
        userRepository.save(user);
        return "User registered successfully";
    }

    public String loginUser(LoginRequestDTO loginRequestDTO){

       Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
       if(authentication.isAuthenticated()){
           return "";
       }
       return"";
    }
}
