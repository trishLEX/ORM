package ru.bmstu.ORM.Service.Session;

import ru.bmstu.ORM.Service.ColumnAnnotations.Column;
import ru.bmstu.ORM.Service.ColumnAnnotations.PK;
import ru.bmstu.ORM.Service.ColumnAnnotations.Table;
import ru.bmstu.ORM.Service.Session.Clauses.SelectClause;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Session implements AutoCloseable {
    private final String connectionString;
    private Connection connection;
    private boolean isClosed;

    Session(String connectionString) {
        this.connectionString = connectionString;
        this.isClosed = true;
    }

    void open() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString);
            isClosed = false;
        } catch (ClassNotFoundException | SQLException classException) {
            classException.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
            isClosed = true;
        } catch (SQLException exception) {
            throw new RuntimeException("Error while closing connection", exception);
        }
    }

    public boolean isClosed() {
        return isClosed;
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

            where(entity, selectString);

            selectString.append(")");
            ResultSet resultSet = stmt.executeQuery(selectString.toString());
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (SQLException | IllegalAccessException exception) {
            throw new RuntimeException("Can't perform contains()", exception);
        }
    }

    public void save(Entity entity) {
        try {
            Statement stmt = connection.createStatement();
            Table tableAnnotation = entity.getClass().getAnnotation(Table.class);
            String table = tableAnnotation.db() + "." + tableAnnotation.schema() + "." + tableAnnotation.name();
            StringBuilder insertString = new StringBuilder(String.format("INSERT INTO %s ( ", table));
            List<Object> values = new ArrayList<>();

            boolean wasFirst = false;
            for (Field field: entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    field.setAccessible(true);
                    values.add(field.get(entity));
                    if (!wasFirst) {
                        insertString.append(field.getAnnotation(Column.class).name());
                        wasFirst = true;
                    } else {
                        insertString.append(", ").append(field.getAnnotation(Column.class).name());
                    }
                }
            }

            wasFirst = false;
            insertString.append(" ) VALUES ( ");
            for (Object value: values) {
                if (!wasFirst) {
                    insertString.append(objectToSQLString(value));
                    wasFirst = true;
                } else {
                    insertString.append(", ").append(objectToSQLString(value));
                }
            }

            insertString.append(" )");
            stmt.execute(insertString.toString());
        } catch (SQLException | IllegalAccessException exception) {
            throw new RuntimeException("Can't save object", exception);
        }
    }

    public void update(Entity entity) {
        try {
            Statement stmt = connection.createStatement();
            Table tableAnnotation = entity.getClass().getAnnotation(Table.class);
            String table = tableAnnotation.db() + "." + tableAnnotation.schema() + "." + tableAnnotation.name();
            StringBuilder updateString = new StringBuilder(String.format("UPDATE %s SET ", table));
            Map<String, String> keys = new HashMap<>();

            boolean wasFirst = false;
            for (Field field: entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(PK.class)) {
                        keys.put(field.getAnnotation(Column.class).name(), objectToSQLString(field.get(entity)));
                    }

                    if (!wasFirst) {
                        updateString.append(field.getAnnotation(Column.class).name()).append(" = ").append(objectToSQLString(field.get(entity)));
                        wasFirst = true;
                    } else {
                        updateString.append(", ").append(field.getAnnotation(Column.class).name()).append(" = ").append(objectToSQLString(field.get(entity)));
                    }
                }
            }

            updateString.append(" WHERE ");
            wasFirst = false;
            for (Map.Entry<String, String> entry: keys.entrySet()) {
                if (!wasFirst) {
                    updateString.append(entry.getKey()).append(" = ").append(entry.getValue());
                    wasFirst = true;
                } else {
                    updateString.append(" AND ").append(entry.getKey()).append(" = ").append(entry.getValue());
                }
            }

            stmt.execute(updateString.toString());
        } catch (SQLException | IllegalAccessException exception) {
            throw new RuntimeException("Can't save object", exception);
        }
    }

    public void delete(Entity entity) {
        try {
            Statement stmt = connection.createStatement();
            Table tableAnnotation = entity.getClass().getAnnotation(Table.class);
            String table = tableAnnotation.db() + "." + tableAnnotation.schema() + "." + tableAnnotation.name();
            StringBuilder deleteString = new StringBuilder(String.format("DELETE FROM %s WHERE ", table));

            where(entity, deleteString);

            stmt.execute(deleteString.toString());
        } catch (SQLException | IllegalAccessException exception) {
            throw new RuntimeException("Can't delete object", exception);
        }
    }

    private void where(Entity entity, StringBuilder string) throws IllegalAccessException {
        boolean wasFirst = false;
        for (Field field: entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PK.class)) {
                field.setAccessible(true);
                if (!wasFirst) {
                    string.append(field.getAnnotation(Column.class).name()).append(" = ").append(objectToSQLString(field.get(entity)));
                    wasFirst = true;
                } else {
                    string.append(" AND ").append(field.getAnnotation(Column.class).name()).append(" = ").append(objectToSQLString(field.get(entity)));
                }
            }
        }
    }

    private String objectToSQLString(Object object) {
        if (object == null) {
            return "NULL";
        } else if (object.getClass() == String.class) {
            return "'" + object + "'";
        } else if (object.getClass().equals(ArrayList.class)) {
            return "ARRAY" + object.toString();
        } else if (object.getClass().equals(Date.class)) {
            return "'" + object + "'::DATE";
        } else if (object.getClass().equals(Time.class)) {
            return "'" + object + "'::TIME";
        } else if (object.getClass().equals(Timestamp.class)) {
            return  "'" + object + "'::TIMESTAMP";
        } else {
            return object.toString();
        }
    }
}
