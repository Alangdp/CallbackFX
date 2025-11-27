package com.connectasistemas.framework.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;          // UUID
    private String name;
    private Integer age;
    private String studentId;
    private String passwordHash;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String name, Integer age, String studentId,
                String passwordHash) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.age = age;
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
