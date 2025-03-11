package com.example.notes.config;

/**
 * UserNotesException
 */
public class UserNotesException extends Exception {

  // Default Serial Version UID
  private static final long serialVersionUID = 1L;

  // Source module that throws exception
  private String source;

  public UserNotesException(String src, String message, Exception e) {
    super(src + "::" + message, e);
    this.source = src;
  }

  public UserNotesException(String src, String message) {
    super(src + "::" + message);
    this.source = src;
  }

  public UserNotesException(String src, Exception e) {
    super(src, e);
    this.source = src;
  }

  public String getSource() {
    return source;
  }
}
