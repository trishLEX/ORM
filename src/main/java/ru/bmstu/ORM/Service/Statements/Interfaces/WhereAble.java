package ru.bmstu.ORM.Service.Statements.Interfaces;

import ru.bmstu.ORM.Service.Statements.Clauses.WhereClause;
import ru.bmstu.ORM.Tables.Entity;

public interface WhereAble<T extends Entity> {
    WhereClause<T> where(String whereClause);
}
