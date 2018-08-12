package ru.bmstu.ORM.Service.Session.Interfaces;

import ru.bmstu.ORM.Service.Session.Clauses.OrderByClause;
import ru.bmstu.ORM.Service.Tables.Entity;

public interface OrderByAble<T extends Entity> {
    OrderByClause<T> orderBy(String orderByClause);
}
