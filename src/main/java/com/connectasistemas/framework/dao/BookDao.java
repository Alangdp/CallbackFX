package com.connectasistemas.framework.dao;

import com.connectasistemas.framework.models.Book;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * DAO para livros
 */
public interface BookDao {

    @SqlQuery("SELECT seq + 1 FROM sqlite_sequence WHERE name = 'book'")
    Integer nextId();

    @SqlUpdate("""
        INSERT INTO book (title, author, publisher, year, isbn, pages, cover_path, available, created_at)
        VALUES (:title, :author, :publisher, :year, :isbn, :pages, :coverPath, :available, :createdAt)
    """)
    void insert(@BindBean Book book);

    @SqlQuery("SELECT * FROM book WHERE id = :id")
    @RegisterBeanMapper(Book.class)
    Book find(@Bind("id") int id);

    @SqlQuery("SELECT * FROM book")
    @RegisterBeanMapper(Book.class)
    List<Book> findAll();

    @SqlUpdate("""
        UPDATE book SET
            title = :title,
            author = :author,
            publisher = :publisher,
            year = :year,
            isbn = :isbn,
            pages = :pages,
            cover_path = :coverPath,
            available = :available,
            created_at = :createdAt
        WHERE id = :id
    """)
    void update(@BindBean Book book);

    @SqlUpdate("DELETE FROM book WHERE id = :id")
    void delete(int id);
}
