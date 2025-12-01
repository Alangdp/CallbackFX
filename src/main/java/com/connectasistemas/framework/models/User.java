package com.connectasistemas.framework.models;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.connectasistemas.framework.utils.DateTimeUtils;
import com.connectasistemas.framework.utils.PasswordAuthentication;

/**
 * Modelo de Usuário
 */
public class User {

    private Integer id;
    private String name;
    private Integer age;
    private String studentId;
    private String passwordHash;
    private String createdAt;
    @ColumnName("is_admin")
    private boolean admin;

    public User() {
        this.createdAt = DateTimeUtils.currentTimestamp();
        this.admin = false;
    }

    public User(String name, Integer age, String studentId,
            String password) {
        this(name, age, studentId, password, false);
    }

    public User(String name, Integer age, String studentId,
            String password, boolean admin) {
        this();
        PasswordAuthentication pa = new PasswordAuthentication();
        String passwordHash = pa.hash(password.toCharArray());

        this.name = name;
        this.age = age;
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.admin = admin;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = DateTimeUtils.normalizeTimestamp(createdAt);
    }

    @ColumnName("is_admin")
    public boolean isAdmin() {
        return admin;
    }

    @ColumnName("is_admin")
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
