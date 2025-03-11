package com.example.notes.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.notes.Constants;
import com.example.notes.config.UserNotesException;
import com.example.notes.dto.BaseUserNoteDTO;
import com.example.notes.dto.UserNoteDTO;
import com.example.notes.service.UserNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "UserNote", description = "User's Note API")
@RestController
@RequestMapping(Constants.BASE_URL)
@SecurityScheme(type = SecuritySchemeType.APIKEY, name = "JSESSIONID", in = SecuritySchemeIn.COOKIE)
@SecurityRequirement(name = "JSESSIONID")
public class UserNoteController {

  @Autowired
  private UserNoteService noteService;

  /**
   * Get a list of all notes for the authenticated user.
   * 
   * @param authentication Authentication
   * @return List with UserNoteDTO for given user
   * @throws UserNotesException
   */
  @Operation(summary = "Get All Notes",
      description = "Get a list of all notes for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @GetMapping(Constants.BASE_NOTES_URL)
  public List<UserNoteDTO> getAll(Authentication authentication) throws UserNotesException {
    return noteService.findAll(authentication.getName());
  }

  /**
   * Get a note by ID for the authenticated user.
   * 
   * @param authentication Authentication
   * @return UserNoteDTO
   * 
   * @throws UserNotesException
   */
  @Operation(summary = "Find Note", description = "Get a note by ID for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @GetMapping(Constants.BASE_NOTES_URL + "/{id}")
  public UserNoteDTO get(Authentication authentication, @PathVariable String id)
      throws UserNotesException {
    return noteService.find(authentication.getName(), id);
  }

  /**
   * Create a new note for the authenticated user.
   * 
   * @param authentication Authentication
   * @param req Request body with new note
   * @return UserNoteDTO
   * @throws UserNotesException
   */
  @Operation(summary = "Create Note", description = "Create a new note for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @PostMapping(Constants.BASE_NOTES_URL)
  public UserNoteDTO create(Authentication authentication, @Valid @RequestBody BaseUserNoteDTO dto)
      throws UserNotesException {
    return noteService.add(authentication.getName(), dto.note());
  }

  /**
   * Update an existing note by ID for the authenticated user.
   * 
   * @param authentication Authentication
   * @param req Request body with new note
   * @return UserNoteDTO
   * @throws UserNotesException
   */
  @Operation(summary = "Update Note",
      description = "Update an existing note by ID for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @PutMapping(Constants.BASE_NOTES_URL)
  public UserNoteDTO update(Authentication authentication, @Valid @RequestBody UserNoteDTO dto)
      throws UserNotesException {
    return noteService.update(authentication.getName(), dto);
  }

  /**
   * Delete a note by ID for the authenticated user.
   * 
   * @param authentication Authentication
   * @param id
   * @throws UserNotesException
   */
  @Operation(summary = "Delete Note",
      description = "Delete a note by ID for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @DeleteMapping(Constants.BASE_NOTES_URL + "/{id}")
  public void delete(Authentication authentication, @PathVariable String id)
      throws UserNotesException {
    noteService.delete(authentication.getName(), id);
  }

  /**
   * Share a note with another user for the authenticated user.
   * 
   * @param authentication Authentication
   * @param dto UserNoteDTO
   * @param id User Id to share with
   * 
   * @throws UserNotesException
   */
  @Operation(summary = "Share Note",
      description = "Share a note with another user for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @PostMapping(Constants.BASE_NOTES_URL + "/{noteId}/share/{userId}")
  public void share(Authentication authentication, @PathVariable String noteId,
      @PathVariable String userId) throws UserNotesException {
    noteService.share(authentication.getName(), noteId, userId);
  }

  /**
   * Search for notes based on keywords for the authenticated user.
   * 
   * @param authentication Authentication
   * @param query Search Query
   * @return List of UserNoteDTO
   * @throws UserNotesException
   */
  @Operation(summary = "Search By Query",
      description = "Search for notes based on keywords for the authenticated user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation")})
  @GetMapping(Constants.SEARCH_NOTES_URL)
  public List<UserNoteDTO> search(Authentication authentication,
      @RequestParam(required = true) String query) throws UserNotesException {
    return noteService.search(authentication.getName(), query);
  }
}
