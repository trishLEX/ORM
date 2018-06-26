package ru.bmstu.ORM.Analyzer.Symbols;

public abstract class Symbol {
    private SymbolType tag;

    public SymbolType getTag() {
        return tag;
    }

    public void setTag(SymbolType tag) {
        this.tag = tag;
    }
}
