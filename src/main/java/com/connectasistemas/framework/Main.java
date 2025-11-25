package com.connectasistemas.framework;

import com.connectasistemas.framework.dao.UsuarioDao;
import com.connectasistemas.framework.models.Usuario;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class Main {
    public static void main(String[] args) {
    // Cria instância do Jdbi
            Jdbi jdbi = Jdbi.create("jdbc:sqlite:app.db");

    // Instala o plugin de SQL Object
            jdbi.installPlugin(new SqlObjectPlugin());

    // Usa o DAO
            UsuarioDao dao = jdbi.onDemand(UsuarioDao.class);



    // Cria tabela
            dao.createTable();
            dao.limpar();

    // Insere
            dao.insert(10);

    // Busca
            Usuario u = dao.find(10);
            System.out.println(u.getId());

    }
}
