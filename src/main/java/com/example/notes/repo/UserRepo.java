/**
 * 
 */
package com.example.notes.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.notes.model.User;

/**
 * MongoDB Repository for user collection
 */
@Repository
public interface UserRepo extends MongoRepository<User, String> {

  User findByName(String name);
}
