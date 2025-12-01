package com.connectasistemas.framework;

import com.connectasistemas.framework.dao.BookDao;
import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.dao.UserDao;
import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.models.User;
import com.connectasistemas.framework.utils.DatabaseManager;
import com.connectasistemas.framework.utils.migrations.MigrationRunner;

public class Main {
    public static void main(String[] args) {
        // Cria instância do Jdbi
        MigrationRunner.run(DatabaseManager.get());
        UserDao userDao = DaoFactory.dao(UserDao.class);
        BookDao bookDao = DaoFactory.dao(BookDao.class);

        // Book 1 – The Way of Kings
        Book b1 = new Book(
                "The Way of Kings",
                "Brandon Sanderson",
                "Tor Books",
                2010,
                "9780765326355",
                1007,
                "covers/way_of_kings.jpg",
                true
        );

        // Book 2 – Words of Radiance
        Book b2 = new Book(
                "Words of Radiance",
                "Brandon Sanderson",
                "Tor Books",
                2014,
                "9780765326362",
                1088,
                "covers/words_of_radiance.jpg",
                true
        );

        // Book 3 – Oathbringer
        Book b3 = new Book(
                "Oathbringer",
                "Brandon Sanderson",
                "Tor Books",
                2017,
                "9780765326379",
                1248,
                "covers/oathbringer.jpg",
                true
        );

        // Inserção
        int g1 = bookDao.nextId();
        b1.setGroupCode(g1);
        b1.setSequence(0);
        bookDao.insert(b1);

        int g2 = bookDao.nextId();
        b2.setGroupCode(g2);
        b2.setSequence(0);
        bookDao.insert(b2);

        int g3 = bookDao.nextId();
        b3.setGroupCode(g3);
        b3.setSequence(0);
        bookDao.insert(b3);

        User userTest1 = new User("Alan", 20, "0420527", "teste123", true);
        User userTest2 = new User("Alan", 20, "0420528", "teste123", false);

        userDao.insert(userTest1);
        userDao.insert(userTest2);
    }
}
