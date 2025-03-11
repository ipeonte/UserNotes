package com.example.notes.security;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.notes.model.User;
import com.example.notes.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@Service
@RateLimiter(name = "login")
public class UserSecurityService implements UserDetailsService {

  @Autowired
  private UserService userService;

  @Override
  public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
    User user;
    try {
      user = userService.findUser(name);
    } catch (Exception e) {
      throw new UsernameNotFoundException("Error find user: " + name, e);
    }

    if (user == null)
      throw new UsernameNotFoundException("User [" + name + "] not found");

    return new org.springframework.security.core.userdetails.User(user.getId(), user.getPassword(),
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
