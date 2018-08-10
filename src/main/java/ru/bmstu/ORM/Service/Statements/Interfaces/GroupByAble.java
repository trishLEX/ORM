package ru.bmstu.ORM.Service.Statements.Interfaces;

import ru.bmstu.ORM.Service.Statements.Clauses.GroupByClause;
import ru.bmstu.ORM.Tables.Entity;

public interface GroupByAble<T extends Entity>  {
    GroupByClause<T> groupBy(String groupByClause);
}
