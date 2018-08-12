package ru.bmstu.ORM.Service.Session;

import ru.bmstu.ORM.Service.Statements.SelectStmt;
import ru.bmstu.ORM.Tables.Entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Session implements AutoCloseable {
    private final String connectionString;
    private Connection connection;

    public Session(String host, String port, String user, String catalog, String password) {
        this.connectionString = "jdbc:postgresql://" +
                host + ":" +
                port + "/" +
                catalog +
                "?user=" + user +
                "&password=" + password;
    }

    public void open() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString);
        } catch (ClassNotFoundException | SQLException classException) {
            classException.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new RuntimeException("Error while closing connection", exception);
        }
    }

    public <T extends Entity> SelectStmt selectFrom(Class<T> table) {
        return new SelectStmt<>(connection, table);
    }
}
