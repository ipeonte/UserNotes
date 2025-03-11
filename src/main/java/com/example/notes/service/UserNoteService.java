package com.example.notes.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.notes.config.UserNotesException;
import com.example.notes.dto.UserNoteDTO;
import com.example.notes.model.UserNote;
import com.example.notes.repo.UserNoteRepo;
import io.github.resilience4j.ratelimiter.RateLimiter.EventPublisher;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;

/**
 * UserNote Service
 * 
 * @author Igor Peonte <igor.144@gmail.com>
 */
@Service
@RateLimiter(name = "api")
public class UserNoteService {

  private static Logger LOG = LoggerFactory.getLogger(UserNoteService.class);

  @Autowired
  private RateLimiterRegistry registry;

  @Autowired
  private UserNoteRepo unRepo;

  /**
   * Get all notes for given user where it's set to owner or included into user's list
   * 
   * @param name
   * @return list of notes for given user
   * 
   * @throws UserNotesException
   */
  public List<UserNoteDTO> findAll(String name) throws UserNotesException {
    LOG.info("Searching all notes for user: {}", name);

    try {
      List<UserNoteDTO> result = mapUserNotes(unRepo.findAllForUser(name));
      LOG.debug("Found {} notes for user: {}", result.size(), name);
      return result;
    } catch (Exception e) {
      throw new UserNotesException("findAll", e);
    }
  }

  /**
   * Create new note for given user
   * 
   * @param name Owner Id
   * @param text UserNote text
   * @return new UserNoteDTO
   * 
   * @throws UserNotesException
   */
  public UserNoteDTO add(String name, String text) throws UserNotesException {
    LOG.info("Adding new notes for user: {}", name);

    try {
      UserNote result = unRepo.save(new UserNote(name, text));
      LOG.debug("Saved new note with id: {} for user: {}", result.getId(), name);
      return new UserNoteDTO(result.getId(), text);
    } catch (Exception e) {
      throw new UserNotesException("add", e);
    }
  }

  /**
   * Find existing note for given user by id
   * 
   * @param name Owner Id
   * @param id Note Id
   * @return existing UserNote
   * 
   * @throws UserNotesException
   */
  public UserNoteDTO find(String name, String id) throws UserNotesException {
    return mapUserNote(findAny(name, id));
  }

  /**
   * Update existing note for given user by id
   * 
   * @param name Owner Id
   * @param dto UserNoteDTO
   * @return Updated UserNoteDTO
   * @throws UserNotesException
   */
  public UserNoteDTO update(String name, UserNoteDTO dto) throws UserNotesException {
    LOG.info("Update existing notes by id: {} for user: {}", dto.id(), name);

    // Find existing note by id
    UserNote un = findByOwner(name, dto.id());
    un.setNote(dto.note());

    try {
      UserNote result = unRepo.save(un);
      LOG.debug("Saved updated note with id: {} for user: {}", result.getId(), name);
      return mapUserNote(un);
    } catch (Exception e) {
      throw new UserNotesException("update", e);
    }
  }

  /**
   * Delete record for user by id
   * 
   * @param name Owner Id
   * @param id Note Id
   * @throws UserNotesException
   */
  public void delete(String name, String id) throws UserNotesException {
    LOG.info("Delete existing notes by id: {} for user: {}", id, name);

    // Check if requested note exist and user is the owner
    findByOwner(name, id);

    try {
      unRepo.deleteById(id);
    } catch (Exception e) {
      throw new UserNotesException("delete", e);
    }
  }


  public void share(String name, String noteId, String userId) throws UserNotesException {
    LOG.info("Share existing notes by id: {} for user: {} with user id: {}", noteId, name, userId);

    // Find existing note by id
    UserNote un = findByOwner(name, noteId);
    un.addUser(userId);

    try {
      UserNote result = unRepo.save(un);
      LOG.debug("Saved updated note after shared with user id: {} with id: {} for user: {}", userId,
          result.getId(), name);
    } catch (Exception e) {
      throw new UserNotesException("share", e);
    }

  }

  public List<UserNoteDTO> search(String name, String query) throws UserNotesException {
    LOG.info("Searching for query: [{}] for user: {}", query, name);
    List<UserNote> result;

    try {
      result = unRepo.findByQuery(name, query);
      LOG.debug("Found {} records by query : [{}] for user: {}", result.size(), query, name);
      return mapUserNotes(result);
    } catch (Exception e) {
      throw new UserNotesException("search", e);
    }
  }

  /**
   * Find existing note for given user by id where user is the owner
   * 
   * @param name Owner Id
   * @param id Note Id
   * @return existing UserNote
   * 
   * @throws UserNotesException
   */
  private UserNote findByOwner(String name, String id) throws UserNotesException {
    LOG.info("Searching for specific existing note by owner id: {} for user: {}", id, name);
    UserNote result;
    try {
      result = unRepo.findByOwnerAndId(name, id);
    } catch (Exception e) {
      throw new UserNotesException("find", e);
    }

    if (result == null)
      throw new UserNotesException("find", "Can't find note with id: " + id + " for user: " + name);

    LOG.debug("Found note with id: {} for user: {}", id, name);
    return result;
  }

  /**
   * Find existing note for given user by id where user has access either as owner or by added as
   * shared user
   * 
   * @param name User Name
   * @param id Note Id
   * @return existing UserNote
   * 
   * @throws UserNotesException
   */
  private UserNote findAny(String name, String id) throws UserNotesException {
    LOG.info("Searching for any existing note by id: {} for user: {}", id, name);
    UserNote result;
    try {
      result = unRepo.findByOwnerAndIdOrUsersAndId(name, id);
    } catch (Exception e) {
      throw new UserNotesException("find", e);
    }

    if (result == null)
      throw new UserNotesException("find", "Can't find note with id: " + id + " for user: " + name);

    LOG.debug("Found note with id: {} for user: {}", id, name);
    return result;
  }


  private UserNoteDTO mapUserNote(UserNote un) {
    return new UserNoteDTO(un.getId(), un.getNote());
  }

  private List<UserNoteDTO> mapUserNotes(Collection<UserNote> list) {
    return list.stream().map(un -> new UserNoteDTO(un.getId(), un.getNote()))
        .collect(Collectors.toList());
  }

  @PostConstruct
  public void registerRateLimiterEvents() {
    EventPublisher eventPublisher = registry.rateLimiter("api").getEventPublisher();
    eventPublisher.onFailure(event -> LOG.warn(event.toString()));
  }
}
