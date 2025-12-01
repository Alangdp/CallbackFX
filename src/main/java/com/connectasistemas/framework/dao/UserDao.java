package com.connectasistemas.framework.dao;

import com.connectasistemas.framework.models.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * DAO para usuários
 */
public interface UserDao {

    @SqlQuery("SELECT seq + 1 FROM sqlite_sequence WHERE name = 'user'")
    Integer nextId();

    /**
     * Insere um user (id deve ser UUID string)
     */
    @SqlUpdate("""
                INSERT INTO user (id, name, age, student_id, password_hash, is_admin, created_at)
                VALUES (:id, :name, :age, :studentId, :passwordHash, :admin, :createdAt)
            """)
    void insert(@BindBean User user);

    /**
     * Busca um user por id
     */
    @SqlQuery("SELECT * FROM user WHERE id = :id")
    @RegisterBeanMapper(User.class)
    User find(@Bind("id") String id);

    /**
     * Busca um user por studentId
     */
    @SqlQuery("SELECT * FROM user WHERE student_id = :studentId")
    @RegisterBeanMapper(User.class)
    User findByStudentId(@Bind("studentId") String studentId);

    /**
     * Retorna todos os users
     */
    @SqlQuery("SELECT * FROM user")
    @RegisterBeanMapper(User.class)
    List<User> findAll();

    /**
     * Atualiza campos de um user (usa id do bean)
     */
    @SqlUpdate("""
                UPDATE user SET
                    name = :name,
                    age = :age,
                    student_id = :studentId,
                    password_hash = :passwordHash,
                    is_admin = :admin,
                    created_at = :createdAt
                WHERE id = :id
            """)
    void update(@BindBean User user);

    /**
     * Deleta um user por id
     */
    @SqlUpdate("DELETE FROM user WHERE id = :id")
    void delete(@Bind("id") String id);
}

