package ru.bmstu.ORM.Service.Statements.Interfaces;

import ru.bmstu.ORM.Service.Annotations.Column;
import ru.bmstu.ORM.Service.Annotations.FK;
import ru.bmstu.ORM.Service.Statements.SelectStmt;
import ru.bmstu.ORM.Tables.Entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Fetchable<T extends Entity> {
    private String selectStmt;
    private Connection connection;
    private Class<T> tableClass;

    public Fetchable(Connection connection, Class<T> tableClass) {
        this.connection = connection;
        this.tableClass = tableClass;
        this.selectStmt = "";
    }

    public Fetchable(Connection connection, Class<T> tableClass, String selectStmt) {
        this.connection = connection;
        this.tableClass = tableClass;
        this.selectStmt = selectStmt;
    }

    public String getSelectStmt() {
        return selectStmt;
    }

    public void setSelectStmt(String selectStmt) {
        this.selectStmt = selectStmt;
    }

    public Connection getConnection() {
        return connection;
    }

    public Class<T> getTableClass() {
        return tableClass;
    }

    public List<T> fetchAll() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectStmt);
            ArrayList<T> result = new ArrayList<>();
            while (resultSet.next()) {
                try {
                    T obj = tableClass.getDeclaredConstructor().newInstance();

                    fillField(resultSet, obj);

                    result.add(obj);

                } catch (InstantiationException | InvocationTargetException
                        | NoSuchMethodException | IllegalAccessException exception) {
                    throw new RuntimeException("Error to fetch", exception);
                }
            }
            return result;
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error to fetch", sqlException);
        }
    }

    public T fetchFirst() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectStmt);
            resultSet.next();
            try {
                T obj = tableClass.getDeclaredConstructor().newInstance();

                fillField(resultSet, obj);

                return obj;

            } catch (InstantiationException | InvocationTargetException
                    | NoSuchMethodException | IllegalAccessException exception) {
                throw new RuntimeException("Error to fetch", exception);
            }

        } catch (SQLException sqlException) {
            throw new RuntimeException("Error to fetch from " + selectStmt, sqlException);
        }
    }

    private void fillField(ResultSet resultSet, T obj) throws IllegalAccessException, SQLException {
        boolean wasFK = false;
        Map<String, Serializable> FKs = new HashMap<>();
        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                field.setAccessible(true);
                field.set(obj, resultSet.getObject(column.name()));

                if (field.isAnnotationPresent(FK.class)) {
                    wasFK = true;
                    FKs.put(field.getAnnotation(FK.class).table(), (Serializable) resultSet.getObject(column.name()));
                }
            }
        }

        if (wasFK) {
            for (Field field : tableClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(FK.class) && !field.isAnnotationPresent(Column.class)) {
                    Class referenced = field.getType();
                    SelectStmt selectStmt = new SelectStmt<>(getConnection(), referenced);
                    Object foreignObject = selectStmt.getById(FKs.get(field.getAnnotation(FK.class).table()));
                    field.setAccessible(true);
                    field.set(obj, foreignObject);
                }
            }
        }
    }
}
