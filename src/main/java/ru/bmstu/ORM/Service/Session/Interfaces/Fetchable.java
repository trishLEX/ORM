package ru.bmstu.ORM.Service.Session.Interfaces;

import ru.bmstu.ORM.Service.ColumnAnnotations.Column;
import ru.bmstu.ORM.Service.ColumnAnnotations.FK;
import ru.bmstu.ORM.Service.ColumnAnnotations.FO;
import ru.bmstu.ORM.Service.Session.Clauses.SelectClause;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
            boolean res = resultSet.next();

            if (!res)
                return null;

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
        Map<String, Map<String, Serializable>> FKs = new HashMap<>();
        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                field.setAccessible(true);
                if (field.getType().equals(ArrayList.class)) {
                    field.set(obj, new ArrayList<>(Arrays.asList((Object[])resultSet.getArray(column.name()).getArray())));
                } else {
                    field.set(obj, resultSet.getObject(column.name()));
                }

                if (field.isAnnotationPresent(FK.class)) {
                    String foreignTableName = field.getAnnotation(FK.class).table();
                    if (!FKs.containsKey(foreignTableName))
                        FKs.put(foreignTableName, new HashMap<>());
                    Map<String, Serializable> foreignTable = FKs.get(foreignTableName);
                    wasFK = true;
                    foreignTable.put(field.getAnnotation(FK.class).referencedColumn(), (Serializable) resultSet.getObject(column.name()));
                }
            }
        }

        if (wasFK) {
            for (Field field : tableClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(FO.class)) {
                    Class referenced = field.getType();
                    SelectClause selectClause = new SelectClause<>(getConnection(), referenced);
                    Object foreignObject = selectClause.getById(FKs.get(field.getAnnotation(FO.class).table()));
                    field.setAccessible(true);
                    field.set(obj, foreignObject);
                }
            }
        }
    }
}
