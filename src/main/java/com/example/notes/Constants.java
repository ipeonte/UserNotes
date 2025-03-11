package com.example.notes;

public class Constants {
  // Default cookie domain that being set by authentication server
  public static final String DEF_COOKIE_DOMAIN = "localhost";

  // Default cookie path that was set by authentication server
  public static final String DEF_COOKIE_PATH = "/";

  // Predefined URL
  public static final String BASE_URL = "/api";
  public static final String BASE_AUTH = "/auth";
  public static final String LOGIN_URL = BASE_AUTH + "/login";
  public static final String SIGNUP_URL = BASE_AUTH + "/signup";
  public static final String BASE_NOTES_URL = "/notes";
  public static final String SEARCH_NOTES_URL = "/search";
}
