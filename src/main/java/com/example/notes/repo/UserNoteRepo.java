/**
 * 
 */
package com.example.notes.repo;

import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.notes.model.UserNote;

/**
 * MongoDB Repository for userNote collection
 */
@Repository
public interface UserNoteRepo extends MongoRepository<UserNote, String> {

  UserNote findByOwnerAndId(String name, String id);

  @Query(value = "{$or:[{'owner':'?0'},{'users':'?0'}]}")
  Collection<UserNote> findAllForUser(String name);

  @Query(value = "{$and:[{$or:[{'owner':'?0'},{'users':'?0'}]},{'note':{$regex: '?1'}}]}")
  List<UserNote> findByQuery(String name, String query);

  @Query(
      value = "{$or:[{$and:[{'owner':'?0'}, {'id':'?1'}]}, {$and:[{'users':'?0'}, {'id':'?1'}]}]}")
  UserNote findByOwnerAndIdOrUsersAndId(String name, String id);
}
