package com.connectasistemas.framework.dao;

import com.connectasistemas.framework.models.Usuario;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

// DAO básico usando interface + anotações
public interface UsuarioDao {

    // Cria tabela
    @SqlUpdate("CREATE TABLE IF NOT EXISTS usuario (id INTEGER PRIMARY KEY)")
    void createTable();

    // Insere
    @SqlUpdate("INSERT INTO usuario (id) VALUES (:id)")
    void insert(@Bind("id") int id);

    // Busca
    @SqlQuery("SELECT * FROM usuario WHERE id = :id")
    @RegisterBeanMapper(Usuario.class)
    Usuario find(@Bind("id") int id);

    @SqlUpdate("DELETE FROM usuario")
    void limpar();
}

