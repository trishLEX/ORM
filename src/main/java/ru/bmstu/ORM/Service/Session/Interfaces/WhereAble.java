package ru.bmstu.ORM.Service.Session.Interfaces;

import ru.bmstu.ORM.Service.Session.Clauses.WhereClause;
import ru.bmstu.ORM.Service.Tables.Entity;

public interface WhereAble<T extends Entity> {
    WhereClause<T> where(String whereClause);
}
