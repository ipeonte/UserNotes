package com.example.notes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:/test_rate_limiter.properties")
public class RateLimiterTests extends BaseAppTest {

  @Test
  void testRateLimiter() {
    String cookie = createTestUser()[0];

    // First search request should pass
    searchAllNotes(cookie);

    // The second search request should be blocked
    searchAllNotes(cookie, HttpStatus.TOO_MANY_REQUESTS);
  }
}
