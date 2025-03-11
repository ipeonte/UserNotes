package com.example.notes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.notes.api.SignUpController;
import com.example.notes.config.UserNotesException;
import com.example.notes.dto.UserDTO;
import com.example.notes.model.User;
import com.example.notes.repo.UserRepo;

/**
 * User Service
 * 
 * @author Igor Peonte <igor.144@gmail.com>
 */
@Service
public class UserService {

  private static Logger LOG = LoggerFactory.getLogger(SignUpController.class);

  @Autowired
  private UserRepo userRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public User findUser(String name) throws UserNotesException {
    try {
      LOG.debug("Searching for existing user [" + name + "]");
      return userRepo.findByName(name);
    } catch (Exception e) {
      throw new UserNotesException("findUser", "Error sign up new user: " + name, e);
    }
  }

  public synchronized void createUser(UserDTO user) throws UserNotesException {
    if (findUser(user.name()) != null)
      throw new UserNotesException("checkNewUser", "User [" + user.name() + "] already exists");

    try {
      LOG.info("Saving new user [" + user.name() + "]");
      // Encrypt password b4 save
      userRepo.save(new User(user.name(), passwordEncoder.encode(user.password())));
    } catch (Exception e) {
      throw new UserNotesException("createUser", "Error sign up new user: " + user, e);
    }

    LOG.debug("Successfully created new user [" + user.name() + "]");
  }
}
