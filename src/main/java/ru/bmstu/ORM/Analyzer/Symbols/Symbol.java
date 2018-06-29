package ru.bmstu.ORM.Analyzer.Symbols;

import ru.bmstu.ORM.Analyzer.Service.Fragment;
import ru.bmstu.ORM.Analyzer.Service.Position;

public abstract class Symbol {
    private SymbolType tag;
    private Fragment coords;

    public Symbol(SymbolType tag) {
        this.tag = tag;
        coords = Fragment.dummyCoords();
    }

    public Symbol(SymbolType tag, Fragment coords) {
        this.tag = tag;
        this.coords = coords;
    }

    public SymbolType getTag() {
        return tag;
    }

    public Fragment getCoords() {
        return coords;
    }

    public Position getStart() {
        return this.coords.getStart();
    }

    public Position getFollow() {
        return this.coords.getFollow();
    }

    public void setTag(SymbolType tag) {
        this.tag = tag;
    }

    public void setStart(Position start) {
        this.coords.setStart(start);
    }

    public void setFollow(Position follow) {
        this.coords.setFollow(follow);
    }

    public void setCoords(Fragment coords) {
        this.coords = coords;
    }
}
