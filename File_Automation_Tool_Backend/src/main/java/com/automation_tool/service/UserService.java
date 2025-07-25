package com.automation_tool.service;

import com.automation_tool.dto.LoginRequestDTO;
import com.automation_tool.dto.RefreshAndAccessTokenDTO;
import com.automation_tool.dto.UserDTO;
import com.automation_tool.entity.User;
import com.automation_tool.mapper.UserMapper;
import com.automation_tool.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   private final  BCryptPasswordEncoder passwordEncoder;
   private final UserRepository userRepository;
   private final AuthenticationManager authenticationManager;
   private final JWTService jwtService;
   private final FileAutomationToolUserDetailsService userDetailsService;

   public UserService(BCryptPasswordEncoder passwordEncoder,
                      UserRepository userRepository,
                      AuthenticationManager authenticationManager,
                      JWTService jwtService,
                      FileAutomationToolUserDetailsService userDetailsService) {

       this.passwordEncoder = passwordEncoder;
       this.userRepository = userRepository;
       this.authenticationManager = authenticationManager;
       this.jwtService = jwtService;
       this.userDetailsService = userDetailsService;

   }


    public String registerUser(UserDTO userDTO){
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User user =UserMapper.mapToUser(userDTO);
        userRepository.save(user);
        return "User registered successfully";
    }

    public RefreshAndAccessTokenDTO loginUser(LoginRequestDTO loginRequestDTO){

       Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
       if(authentication.isAuthenticated()){
           return jwtService.generateToken(loginRequestDTO.getEmail());
       }
       return new RefreshAndAccessTokenDTO("","","Authentication Failed");
    }

    public HashMap<String,String> generateAccessToken(String refreshToken){

       String email = jwtService.extractEmail(refreshToken);
       UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      if(!jwtService.validateRefreshToken(refreshToken,userDetails)) {
          return new HashMap<>(Map.of("Error","Not a valid refresh token"));
      }
      return jwtService.generateAccessTokenWithRefreshToken(refreshToken);

    }


    public String logoutUser(RefreshAndAccessTokenDTO  refreshAndAccessTokenDTO){

        String accessToken = refreshAndAccessTokenDTO.accessToken();
        String refreshToken = refreshAndAccessTokenDTO.refreshToken();

        logger.info("refresh tokens {} access token {}",refreshToken,accessToken);

        if (accessToken != null) {
            jwtService.blacklistToken(accessToken);
        }
        if (refreshToken != null) {
            jwtService.blacklistToken(refreshToken);
        }

       return "logged out successfully";
    }
}
