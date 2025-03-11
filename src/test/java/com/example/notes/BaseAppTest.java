package com.example.notes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.example.notes.dto.UserDTO;
import com.example.notes.model.User;
import com.example.notes.repo.UserNoteRepo;
import com.example.notes.repo.UserRepo;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:/test_rate_limiter.properties")
class BaseAppTest {

  private static final String TEST_USER = "user1";
  private static final String TEST_PWD = "test1";

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private UserRepo userRepo;

  @Autowired
  private UserNoteRepo unRepo;

  protected String[] createTestUser() {
    return createTestUser(TEST_USER, TEST_PWD);
  }

  protected String[] createTestUser(String name, String password) {
    // Add test user
    String id = addTestUser(name, password);

    // Login with new user
    return new String[] {login(name, password), id};
  }

  protected void searchAllNotes(String cookie) {
    searchAllNotes(cookie, HttpStatus.OK);
  }

  protected void searchAllNotes(String cookie, HttpStatus status) {
    ResponseEntity<String> resp = rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL,
        HttpMethod.GET, getHttpEntity(cookie), String.class);
    assertEquals(status, resp.getStatusCode(), "Search All Notes response status doesn't match.");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected HttpEntity<?> getHttpEntity(String cookie) {
    return new HttpEntity(null, getAuthHttpHeaders(cookie));
  }

  protected HttpHeaders getAuthHttpHeaders(String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", cookie);
    return headers;
  }

  protected String addTestUser(String name, String password) {
    ResponseEntity<String> resp = rest.postForEntity(Constants.BASE_URL + Constants.SIGNUP_URL,
        new UserDTO(name, password), String.class);
    assertEquals(HttpStatus.OK, resp.getStatusCode(), "Signup response doesn't match.");

    // Check user exists
    User user = userRepo.findByName(name);
    assertNotNull(user, "User is expected to exist after signup");

    return user.getId();
  }

  protected String login(String name, String password) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.set("name", name);
    form.set("password", password);
    ResponseEntity<String> resp = rest.postForEntity(Constants.BASE_URL + Constants.LOGIN_URL,
        new HttpEntity<>(form, new HttpHeaders()), String.class);
    assertEquals(HttpStatus.OK, resp.getStatusCode(), "Login response doesn't match.");

    return resp.getHeaders().get("Set-Cookie").get(0);
  }

  @AfterEach
  void cleanup() {
    userRepo.deleteAll();
    unRepo.deleteAll();
  }
}
