package com.example.notes.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.example.notes.config.UserNotesException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@ControllerAdvice
public class UserNotesExceptionHandler {

  private static Logger LOG = LoggerFactory.getLogger(SignUpController.class);

  /**
   * UserNotesException handler
   * 
   * @param ex UserNotesException Exception
   * @return ResponseEntity
   */
  @ExceptionHandler(UserNotesException.class)
  private ResponseEntity<String> handleUserNotesExceptionHandler(UserNotesException ex) {
    // Just log for now
    LOG.error(ex.toString() + ex.getCause() != null ? " - " + ex.toString() : "");

    // And return 500. Not displaying the details, just the source of the problem
    return ResponseEntity.internalServerError().body(ex.getSource() + " Error");
  }

  /**
   * RequestNotPermitted handler
   * 
   * @param ex RequestNotPermitted Exception
   */
  @ExceptionHandler(RequestNotPermitted.class)
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  private void handleRequestNotPermitted(RequestNotPermitted ex) {
    // Just log for now
    LOG.error(ex.toString());
  }
}
