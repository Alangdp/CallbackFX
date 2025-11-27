package com.connectasistemas.framework.utils.migrations;

import com.connectasistemas.framework.interfaces.Migration;
import org.jdbi.v3.core.Jdbi;

/**
 * Migração das tabelas da biblioteca
 */
public class GenericMigration implements Migration {

    // Criação das tabelas user, book e borrow_history
    public void up(Jdbi jdbi) {
        jdbi.useHandle(h -> {
            h.execute("""
            CREATE TABLE IF NOT EXISTS user (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                age INTEGER,
                student_id TEXT,
                password_hash TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """);

            h.execute("""
            CREATE TABLE IF NOT EXISTS book (
                id TEXT PRIMARY KEY,
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

            h.execute("""
            CREATE TABLE IF NOT EXISTS borrow_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                book_id TEXT NOT NULL,
                borrowed_at TEXT,
                returned_at TEXT,
                FOREIGN KEY (user_id) REFERENCES user(id),
                FOREIGN KEY (book_id) REFERENCES book(id)
            )
        """);
        });
    }
}
