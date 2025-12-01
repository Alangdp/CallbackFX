package com.connectasistemas.framework.utils.migrations;

import com.connectasistemas.framework.interfaces.Migration;
import org.jdbi.v3.core.Jdbi;

/**
 * Migração genérica para criar tabelas de usuário, livro e histórico de empréstimos
 */
public class GenericMigration implements Migration {

    @Override
    public void up(Jdbi jdbi) {
        jdbi.useHandle(h -> {

            // user
            h.execute("""
                CREATE TABLE IF NOT EXISTS user (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER,
                    student_id TEXT,
                    password_hash TEXT NOT NULL,
                    is_admin INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);
            h.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_user_id ON user(id)");

            // book
            h.execute("""
                CREATE TABLE IF NOT EXISTS book (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    group_code INTEGER,
                    sequence INTEGER DEFAULT 0,
                    title TEXT NOT NULL,
                    author TEXT,
                    publisher TEXT,
                    year INTEGER,
                    isbn TEXT,
                    pages INTEGER,
                    cover_path TEXT,
                    available BOOLEAN,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);
            h.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_book_id ON book(id)");
            h.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_book_group_sequence ON book(group_code, sequence)");
            try {
                h.execute("ALTER TABLE book ADD COLUMN group_code INTEGER");
            } catch (Exception ignored) {
            }
            try {
                h.execute("ALTER TABLE book ADD COLUMN sequence INTEGER DEFAULT 0");
            } catch (Exception ignored) {
            }
            h.execute("UPDATE book SET group_code = id WHERE group_code IS NULL");
            h.execute("UPDATE book SET sequence = 0 WHERE sequence IS NULL");

            // borrow_history
            h.execute("""
                CREATE TABLE IF NOT EXISTS borrow_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    borrowed_at TEXT,
                    due_at TEXT,
                    returned_at TEXT,
                    FOREIGN KEY (user_id) REFERENCES user(id),
                    FOREIGN KEY (book_id) REFERENCES book(id)
                )
            """);
            h.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_borrow_id ON borrow_history(id)");
            try {
                h.execute("ALTER TABLE borrow_history ADD COLUMN due_at TEXT");
            } catch (Exception ignored) {
            }
            h.execute("UPDATE borrow_history SET due_at = strftime('%Y-%m-%d %H:%M:%S', borrowed_at, '+14 day') WHERE due_at IS NULL OR trim(due_at) = ''");
        });
    }
}
