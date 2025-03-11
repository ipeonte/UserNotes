package com.example.notes.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.notes.Constants;
import com.example.notes.config.UserNotesException;
import com.example.notes.dto.UserDTO;
import com.example.notes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "UserSignUp", description = "UserSignUp API")
@RestController
@RequestMapping(Constants.BASE_URL)
public class SignUpController {

  @Autowired
  private UserService userService;

  /**
   * Create a new user account.
   * 
   * @param user UserDTO
   * @throws UserNotesException
   */
  @Operation(summary = "Create New User", description = "Create a new user account.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @PostMapping("/auth/signup")
  public void createUser(@Valid @RequestBody UserDTO user) throws UserNotesException {
    userService.createUser(user);
  }
}
