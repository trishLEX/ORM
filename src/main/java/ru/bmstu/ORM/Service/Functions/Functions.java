package ru.bmstu.ORM.Service.Functions;

import ru.bmstu.ORM.Service.ColumnAnnotations.Routines;
import ru.bmstu.ORM.Service.Session.Interfaces.FunctionsExecutor;

import java.sql.Connection;
import java.util.ArrayList;

@Routines(db = "shopdb", schema = "shopschema")
public class Functions extends FunctionsExecutor {
    public Functions(Connection connection) {
        super(connection);
    }

    public Integer go() {
        return (Integer) super.executeScalarFunction(this.getClass(), "go()");
    }

    public ArrayList<AFunctionTable> a() {
        return super.executeTableFunction(this.getClass(), AFunctionTable.class, "a()");
    }

    public ArrayList<AFunctionTable> a(int a) {
        return super.executeTableFunction(this.getClass(), AFunctionTable.class, String.format("a(%s)", a));
    }
}
