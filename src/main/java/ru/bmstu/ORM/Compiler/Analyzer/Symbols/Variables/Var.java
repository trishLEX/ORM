package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class Var extends Symbol {
    private ArrayList<Symbol> symbols;

    public Var(VarTag tag) {
        super(tag);
        this.symbols = new ArrayList<>();
    }

    public void addSymbol(Symbol s) {
        this.symbols.add(s);
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }

    public Symbol get(int i) {
        return symbols.get(i);
    }

    public int size() {
        return symbols.size();
    }

    @Override
    public String toString() {
        return this.getTag() + " " + getCoords();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || this.getClass() != obj.getClass())
            return false;

        Var other = (Var) obj;
        if (this.symbols.size() != other.symbols.size())
            return false;

        for (int i = 0; i < this.symbols.size(); i++)
            if (!this.symbols.get(i).equals(other.symbols.get(i)))
                return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Symbol s: symbols) {
            hashCode = 31 * hashCode + s.hashCode();
        }
        return hashCode;
    }
}
