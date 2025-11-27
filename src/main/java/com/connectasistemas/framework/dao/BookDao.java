package com.connectasistemas.framework.dao;

import com.connectasistemas.framework.models.Book;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * DAO mínimo para Book
 */
public interface BookDao {

    /**
     * Insere um book (id deve ser UUID string)
     */
    @SqlUpdate("""
                INSERT INTO book (id, title, author, publisher, year, isbn, pages, cover_path, available, created_at)
                VALUES (:id, :title, :author, :publisher, :year, :isbn, :pages, :coverPath, :available, :createdAt)
            """)
    void insert(@BindBean Book book);

    /**
     * Busca um book por id
     */
    @SqlQuery("SELECT * FROM book WHERE id = :id")
    @RegisterBeanMapper(Book.class)
    Book find(@Bind("id") String id);

    /**
     * Retorna todos os books
     */
    @SqlQuery("SELECT * FROM book")
    @RegisterBeanMapper(Book.class)
    List<Book> findAll();

    /**
     * Atualiza um book (usa id do bean)
     */
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

    /**
     * Deleta um book por id
     */
    @SqlUpdate("DELETE FROM book WHERE id = :id")
    void delete(@Bind("id") String id);
}