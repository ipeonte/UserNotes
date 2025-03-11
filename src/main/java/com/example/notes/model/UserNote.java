package com.example.notes.model;

import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserNote {

  @Id
  String id;

  // Name of the creator and owner
  private String owner;

  // Shared users
  private Set<String> users;

  // User's note. Indexed field
  @TextIndexed
  private String note;

  public UserNote(String owner, String note) {
    this.note = note;
    this.owner = owner;
    this.users = new HashSet<>();
  }

  public String getId() {
    return id;
  }

  public String getOwner() {
    return owner;
  }

  public Set<String> getUsers() {
    return users;
  }

  public void addUser(String user) {
    this.users.add(user);
  }

  public String getNote() {
    return note;
  }

  public void setNote(String notes) {
    this.note = notes;
  }
}
