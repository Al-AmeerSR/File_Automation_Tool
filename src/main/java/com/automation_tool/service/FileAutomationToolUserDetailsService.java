package com.automation_tool.service;

import com.automation_tool.entity.User;
import com.automation_tool.entity.UserPrincipal;
import com.automation_tool.exception.UserNotFoundException;
import com.automation_tool.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FileAutomationToolUserDetailsService implements UserDetailsService {

    private final UserRepository  userRepository;
    public FileAutomationToolUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User Not Found with email: " + email);
        }
        return new UserPrincipal(user) ;
    }
}
