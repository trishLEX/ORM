package ru.bmstu.ORM.Service.Functions;

import ru.bmstu.ORM.Service.ColumnAnnotations.Column;

public class AFunctionTable implements ReturnedTable {
    @Column(name = "col")
    private int col;

    @Column(name = "col1")
    private int col1;

    public void setCol(int col) {
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "AFunctionTable { col: " + col + ", col1: " + col1 + "}";
    }
}
