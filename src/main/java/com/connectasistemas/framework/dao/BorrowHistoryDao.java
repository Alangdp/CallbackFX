package com.connectasistemas.framework.dao;

import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.models.BorrowHistory;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * DAO mínimo para BorrowHistory
 */
public interface BorrowHistoryDao {

    /**
     * Insere um registro de retirada; returned_at pode ser null
     */
    @SqlUpdate("""
                INSERT INTO borrow_history (user_id, book_id, borrowed_at, returned_at)
                VALUES (:userId, :bookId, :borrowedAt, :returnedAt)
            """)
    void insert(@Bind("userId") String userId,
                @Bind("bookId") String bookId,
                @Bind("borrowedAt") String borrowedAt,
                @Bind("returnedAt") String returnedAt);

    /**
     * Busca um registro pelo id
     */
    @SqlQuery("SELECT * FROM borrow_history WHERE id = :id")
    @RegisterBeanMapper(BorrowHistory.class)
    BorrowHistory find(@Bind("id") int id);

    /**
     * Retorna todas as retiradas de um usuário (ordena pela data de retirada desc)
     */
    @SqlQuery("SELECT * FROM borrow_history WHERE user_id = :userId ORDER BY borrowed_at DESC")
    @RegisterBeanMapper(BorrowHistory.class)
    List<BorrowHistory> findByUser(@Bind("userId") String userId);

    /**
     * Retorna todas as retiradas de um usuário desde uma data (inclusive). 'since' em ISO text.
     */
    @SqlQuery("""
                SELECT * FROM borrow_history
                WHERE user_id = :userId
                  AND borrowed_at >= :since
                ORDER BY borrowed_at DESC
            """)
    @RegisterBeanMapper(BorrowHistory.class)
    List<BorrowHistory> findByUserSince(@Bind("userId") String userId, @Bind("since") String since);

    /**
     * Retorna todos os livros (book.*) que um usuário já retirou (distinct)
     */
    @SqlQuery("""
                SELECT DISTINCT b.* FROM book b
                JOIN borrow_history bh ON b.id = bh.book_id
                WHERE bh.user_id = :userId
                ORDER BY bh.borrowed_at DESC
            """)
    @RegisterBeanMapper(Book.class)
    List<Book> findBooksBorrowedByUser(@Bind("userId") String userId);

    /**
     * Marca devolução: define returned_at para o registro
     */
    @SqlUpdate("UPDATE borrow_history SET returned_at = :returnedAt WHERE id = :id")
    void markReturned(@Bind("id") int id, @Bind("returnedAt") String returnedAt);

    /**
     * Deleta todos os registros de borrow_history de um user (uso administrativo)
     */
    @SqlUpdate("DELETE FROM borrow_history WHERE user_id = :userId")
    void deleteByUser(@Bind("userId") String userId);
}