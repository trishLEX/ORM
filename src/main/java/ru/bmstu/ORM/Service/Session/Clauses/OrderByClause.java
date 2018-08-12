package ru.bmstu.ORM.Service.Session.Clauses;

import ru.bmstu.ORM.Service.Session.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.sql.Connection;

public class OrderByClause<T extends Entity> extends Fetchable<T> {
    OrderByClause(Connection connection, Class<T> tableClass, String selectStmt, String orderByClause) {
        super(connection, tableClass, selectStmt + " ORDER BY " + orderByClause);

    }
}
