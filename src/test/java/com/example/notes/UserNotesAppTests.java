package com.example.notes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
import com.example.notes.dto.BaseUserNoteDTO;
import com.example.notes.dto.UserDTO;
import com.example.notes.dto.UserNoteDTO;
import com.example.notes.model.User;
import com.example.notes.repo.UserNoteRepo;
import com.example.notes.repo.UserRepo;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:/test.properties")
class UserNotesAppTests {

  private static final String TEST_USER = "user1";
  private static final String TEST_PWD = "test1";

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private UserRepo userRepo;

  @Autowired
  private UserNoteRepo unRepo;

  /**
   * Test the Global Scenario with all UserNote API
   */
  @Test
  void testGlobalScenario() {
    // Test note text
    final String text1 = "test123";
    final String text2 = "test456";
    final String text3 = "test789";
    final String text4 = "Lorem ipsum dolor sit amet";

    // Test scenario for the first user
    String[] data1 = checkSingleUserScenario(TEST_USER, TEST_PWD, text1, text2, "first user.");
    String cookie1 = data1[0];

    // Test scenario for the second user
    String[] data2 = checkSingleUserScenario("user2", "test2", text1, text3, "second user.");
    String id2 = data2[1];
    String cookie2 = data2[0];

    // Test scenario for the third user
    String[] data3 = createTestUser("user3", "test3");
    String id3 = data3[1];
    String cookie3 = data3[0];

    // Add second note for the first user
    String idx12 = addNewNote(cookie1, text4);

    // Check first user has 2 notes and second user still 1 note
    findAll(cookie1, 2, "first");
    findAll(cookie2, 1, "second");

    // Share second note from first user with second user and third user
    shareNote(cookie1, idx12, id2);
    shareNote(cookie1, idx12, id3);

    // Check second and third users can find same note by id
    findNote(cookie2, idx12, text4);
    findNote(cookie3, idx12, text4);

    // Check both user has 2 notes each but third user only 1 note
    findAll(cookie1, 2, "first user after second note added.");
    findAll(cookie2, 2, "second user after second note shared with second user.");
    findAll(cookie3, 1, "third user after second note shared with third user.");

    // Test different search by query
    search(cookie1, "test", 1, text2);
    search(cookie1, "qq", 0, null);
    search(cookie2, "789", 1, text3);
    search(cookie3, "ipsum", 1, text4);

    // Delete the second note from first user
    deleteNote(cookie1, idx12);

    // Check first and second user's have exactly one note each and third user has no record
    findAll(cookie1, 1, "first user after second note deleted.");
    findAll(cookie2, 1, "second user after second note deleted from first user.");
    findAll(cookie3, 0, "third user after third note deleted from first user.");
  }

  @Test
  void testInvalidScenario() {
    // Create new user and add test note
    String cookie = createTestUser()[0];
    final String noteId = addNewNote(cookie, "test");

    // TODO Test signup with invalid or null DTO
    // TODO Test new note with null body or missing note

    // Test update note with null body
    ResponseEntity<UserNoteDTO> resp = rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL,
        HttpMethod.PUT, getHttpEntity(cookie), UserNoteDTO.class);
    assertNotNull(resp, "Expected non null response.");
    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode(),
        "Response status code doesn't match for update with null body");

    // Add second user
    String cookie2 = createTestUser("user2", "test2")[0];

    // Try search, update or delete for the record that belongs to different user
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
        rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL + "/" + noteId, HttpMethod.GET,
            getHttpEntity(cookie2), String.class).getStatusCode());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
        rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL, HttpMethod.PUT,
            getHttpEntity(cookie2, noteId, "test"), String.class).getStatusCode());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
        rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL + "/" + noteId,
            HttpMethod.DELETE, getHttpEntity(cookie2), String.class).getStatusCode());
  }

  // @Test
  // TODO Move into new file
  void testRateLimiter() {
    String cookie = createTestUser()[0];

    // First search request should pass
    searchAllNotes(cookie);

    // The second search request should be blocked
    searchAllNotes(cookie, HttpStatus.TOO_MANY_REQUESTS);
  }

  private void shareNote(String cookie, String noteId, String userId) {
    assertEquals(HttpStatus.OK,
        rest.exchange(
            Constants.BASE_URL + Constants.BASE_NOTES_URL + "/" + noteId + "/share/" + userId,
            HttpMethod.POST, getHttpEntity(cookie), String.class).getStatusCode());
  }

  private String[] createTestUser() {
    return createTestUser(TEST_USER, TEST_PWD);
  }

  private String[] createTestUser(String name, String password) {
    // Add test user
    String id = addTestUser(name, password);

    // Login with new user
    return new String[] {login(name, password), id};
  }

  private String[] checkSingleUserScenario(final String name, final String password,
      final String text1, final String text2, String userMsg) {
    // Create test user
    String[] data = createTestUser(name, password);
    String cookie1 = data[0];

    // Check user has no notes
    findAll(cookie1, 0, "new " + userMsg);

    // Add new note
    final String noteId = addNewNote(cookie1, text1);

    // Find existing note
    findNote(cookie1, noteId, text1);

    // Update note with different text
    updateNote(cookie1, noteId, text2);

    // Find existing note and check for updated text
    findNote(cookie1, noteId, text2);

    // Check it still one note belong to user
    findAll(cookie1, 1, userMsg);

    return new String[] {data[0], data[1], noteId};
  }

  private void deleteNote(String cookie, String id) {
    assertEquals(HttpStatus.OK,
        rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL + "/" + id, HttpMethod.DELETE,
            getHttpEntity(cookie), String.class).getStatusCode());
  }


  private UserNoteDTO updateNote(String cookie, String id, String text) {
    ResponseEntity<UserNoteDTO> resp = rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL,
        HttpMethod.PUT, getHttpEntity(cookie, id, text), UserNoteDTO.class);

    return checkNote(id, text, resp, "updated");
  }

  private HttpEntity<UserNoteDTO> getHttpEntity(String cookie, String id, String text) {
    return new HttpEntity<UserNoteDTO>(new UserNoteDTO(id, text), getAuthHttpHeaders(cookie));
  }

  private UserNoteDTO findNote(String cookie, String id, String text) {
    ResponseEntity<UserNoteDTO> resp =
        rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL + "/" + id, HttpMethod.GET,
            getHttpEntity(cookie), UserNoteDTO.class);

    return checkNote(id, text, resp, "existing");
  }

  private UserNoteDTO checkNote(String id, String text, ResponseEntity<UserNoteDTO> resp,
      String msg) {
    assertEquals(HttpStatus.OK, resp.getStatusCode(),
        "Response status doesn't match for " + msg + " note");

    UserNoteDTO result = resp.getBody();
    System.out.println(result);
    assertNotNull(result, "Expected non null response body for " + msg + " note");
    assertEquals(id, result.id(), "Note id doesn't match for " + msg + " note");
    assertEquals(text, result.note(), "Note text doesn't match  for " + msg + " note");

    return result;
  }

  private String addNewNote(String cookie, String text) {
    ResponseEntity<UserNoteDTO> resp =
        rest.postForEntity(Constants.BASE_URL + Constants.BASE_NOTES_URL,
            new HttpEntity<BaseUserNoteDTO>(new BaseUserNoteDTO(text), getAuthHttpHeaders(cookie)),
            UserNoteDTO.class);
    assertEquals(HttpStatus.OK, resp.getStatusCode(), "Post new note status doesn't match.");
    UserNoteDTO result = resp.getBody();
    assertNotNull(result);
    assertNotNull(result.id(), "Expected valid id in new note.");
    assertEquals(text, result.note(), "Note text doesn't match.");

    return result.id();
  }

  private UserNoteDTO[] findAll(String cookie, int size, String msg) {
    ResponseEntity<UserNoteDTO[]> resp =
        rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL, HttpMethod.GET,
            getHttpEntity(cookie), UserNoteDTO[].class);

    assertEquals(HttpStatus.OK, resp.getStatusCode(),
        "Search All Notes response status doesn't match.");
    UserNoteDTO[] result = resp.getBody();
    assertNotNull(result);
    assertEquals(size, result.length, "Number of notes doesn't match for " + msg);
    return result;
  }

  private void searchAllNotes(String cookie) {
    searchAllNotes(cookie, HttpStatus.OK);
  }

  private void searchAllNotes(String cookie, HttpStatus status) {
    ResponseEntity<String> resp = rest.exchange(Constants.BASE_URL + Constants.BASE_NOTES_URL,
        HttpMethod.GET, getHttpEntity(cookie), String.class);
    assertEquals(status, resp.getStatusCode(), "Search All Notes response status doesn't match.");
  }

  private void search(String cookie, String query, int size, String text) {
    ResponseEntity<UserNoteDTO[]> resp =
        rest.exchange(Constants.BASE_URL + Constants.SEARCH_NOTES_URL + "?query=" + query,
            HttpMethod.GET, getHttpEntity(cookie), UserNoteDTO[].class);

    assertEquals(HttpStatus.OK, resp.getStatusCode(),
        "Search By Query response status doesn't match.");
    UserNoteDTO[] result = resp.getBody();
    assertNotNull(result);
    assertNotNull(result);
    assertEquals(size, result.length, "Number of search by query notes doesn't match.");

    if (text != null)
      assertEquals(text, result[0].note(), "Note text doesn't match.");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private HttpEntity<?> getHttpEntity(String cookie) {
    return new HttpEntity(null, getAuthHttpHeaders(cookie));
  }

  private HttpHeaders getAuthHttpHeaders(String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", cookie);
    return headers;
  }

  private String addTestUser(String name, String password) {
    ResponseEntity<String> resp = rest.postForEntity(Constants.BASE_URL + Constants.SIGNUP_URL,
        new UserDTO(name, password), String.class);
    assertEquals(HttpStatus.OK, resp.getStatusCode(), "Signup response doesn't match.");

    // Check user exists
    User user = userRepo.findByName(name);
    assertNotNull(user, "User is expected to exist after signup");

    return user.getId();
  }

  private String login(String name, String password) {
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
