package ru.bmstu.ORM.Service.Session.Interfaces;

import ru.bmstu.ORM.Service.ColumnAnnotations.Column;
import ru.bmstu.ORM.Service.ColumnAnnotations.Routines;
import ru.bmstu.ORM.Service.Functions.ReturnedTable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;

public abstract class FunctionsExecutor {
    private Connection connection;

    public FunctionsExecutor(Connection connection) {
        this.connection = connection;
    }

    protected <T extends FunctionsExecutor> void executeVoidFunction(Class<T> clazz, String function) {
        Routines routines = clazz.getAnnotation(Routines.class);
        function = routines.db() + "." + routines.schema() + "." + function;
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.execute("SELECT " + function);
        } catch (SQLException exception) {
            throw new RuntimeException("Can't perform function " + function, exception);
        }
    }

    protected <T extends FunctionsExecutor> Object executeScalarFunction(Class<T> clazz, String function) {
        Routines routines = clazz.getAnnotation(Routines.class);
        function = routines.db() + "." + routines.schema() + "." + function;
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery("SELECT " + function);
            resultSet.next();

            return resultSet.getObject(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Can't perform function " + function, exception);
        }
    }

    protected <T extends FunctionsExecutor, P extends ReturnedTable>
    ArrayList<P> executeTableFunction(Class<T> clazz, Class<P> tableClass, String function) {
        StringBuilder query = new StringBuilder("SELECT ");
        boolean wasFirst = false;
        for (Field field: tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                if (!wasFirst) {
                    query.append(field.getAnnotation(Column.class).name());
                    wasFirst = true;
                } else {
                    query.append(", ").append(field.getAnnotation(Column.class).name());
                }
            }
        }
        Routines routines = clazz.getAnnotation(Routines.class);
        query.append(" FROM ").append(routines.db()).append(".").append(routines.schema()).append(".").append(function);

        ArrayList<P> result = new ArrayList<>();

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(query.toString());

            while (resultSet.next()) {
                P obj = tableClass.getDeclaredConstructor().newInstance();
                for (Field field : tableClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        field.setAccessible(true);
                        field.set(obj, resultSet.getObject(field.getAnnotation(Column.class).name()));
                    }
                }

                result.add(obj);
            }
        } catch (SQLException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException("Can't perform function " + query.toString(), exception);
        }

        return result;
    }
}
