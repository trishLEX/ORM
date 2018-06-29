package ru.bmstu.ORM.Analyzer.Symbols.Variables;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;

import java.util.ArrayList;

public abstract class Var extends Symbol {
    private ArrayList<Symbol> symbols;

    public Var(VarTag tag) {
        super(tag);
        this.symbols = new ArrayList<>();
    }

    public void addSymbol(Symbol s) {
        this.symbols.add(s);
    }

    @Override
    public String toString() {
        return this.getTag() + " " + getCoords();
    }
}
