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

    @SqlQuery("SELECT COALESCE((SELECT seq + 1 FROM sqlite_sequence WHERE name = 'book'), 1)")
    Integer nextId();

    @SqlUpdate("""
        INSERT INTO book (group_code, sequence, title, author, publisher, year, isbn, pages, cover_path, available, created_at)
        VALUES (:groupCode, :sequence, :title, :author, :publisher, :year, :isbn, :pages, :coverPath, :available, :createdAt)
    """)
    void insert(@BindBean Book book);

    @SqlQuery("SELECT * FROM book WHERE id = :id")
    @RegisterBeanMapper(Book.class)
    Book find(@Bind("id") int id);

    @SqlQuery("SELECT * FROM book WHERE isbn = :isbn")
    @RegisterBeanMapper(Book.class)
    Book findByIsbn(@Bind("isbn") String isbn);

    @SqlQuery("SELECT * FROM book")
    @RegisterBeanMapper(Book.class)
    List<Book> findAll();

    @SqlUpdate("""
        UPDATE book SET
            group_code = :groupCode,
            sequence = :sequence,
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
    void delete(@Bind("id") int id);

    @SqlQuery("SELECT * FROM book WHERE group_code = :groupCode ORDER BY sequence")
    @RegisterBeanMapper(Book.class)
    List<Book> findByGroupCode(@Bind("groupCode") int groupCode);

    @SqlQuery("SELECT * FROM book WHERE group_code = :groupCode AND sequence = :sequence")
    @RegisterBeanMapper(Book.class)
    Book findByGroupAndSequence(@Bind("groupCode") int groupCode, @Bind("sequence") int sequence);

    @SqlQuery("SELECT COUNT(*) FROM book WHERE group_code = :groupCode")
    int countByGroupCode(@Bind("groupCode") int groupCode);

    @SqlQuery("SELECT COALESCE(MAX(sequence), -1) FROM book WHERE group_code = :groupCode")
    int maxSequence(@Bind("groupCode") int groupCode);

    @SqlQuery("SELECT * FROM book WHERE group_code = :groupCode AND available = 1 ORDER BY sequence")
    @RegisterBeanMapper(Book.class)
    List<Book> findAvailableByGroup(@Bind("groupCode") int groupCode);
}
