package ru.bmstu.ORM.Service.Session;

import ru.bmstu.ORM.Service.ColumnAnnotations.Column;
import ru.bmstu.ORM.Service.ColumnAnnotations.PK;
import ru.bmstu.ORM.Service.ColumnAnnotations.Table;
import ru.bmstu.ORM.Service.Session.Clauses.SelectClause;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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

    public <T extends Entity> SelectClause<T> selectFrom(Class<T> table) {
        return new SelectClause<>(connection, table);
    }

    public boolean contains(Entity entity) {
        try {
            Statement stmt = connection.createStatement();
            Table tableAnnotation = entity.getClass().getAnnotation(Table.class);
            String table = tableAnnotation.db() + "." + tableAnnotation.schema() + "." + tableAnnotation.name();
            StringBuilder selectString = new StringBuilder(String.format("SELECT EXISTS(SELECT 1 FROM %s WHERE ", table));
            Map<String, Object> keys = new HashMap<>();
            for (Field field: entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(PK.class)) {
                    field.setAccessible(true);
                    keys.put(field.getAnnotation(Column.class).name(), field.get(entity));
                }
            }
            boolean wasFirst = false;
            for (Map.Entry<String, Object> entry: keys.entrySet()) {
                if (!wasFirst) {
                    wasFirst = true;
                    selectString.append(entry.getKey()).append(" = ").append(entry.getValue());
                } else {
                    selectString.append(" AND ").append(entry.getKey()).append(" = ").append(entry.getValue());
                }
            }
            selectString.append(")");
            ResultSet resultSet = stmt.executeQuery(selectString.toString());
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (SQLException | IllegalAccessException exception) {
            throw new RuntimeException("Can't perform contains()", exception);
        }
    }
}
