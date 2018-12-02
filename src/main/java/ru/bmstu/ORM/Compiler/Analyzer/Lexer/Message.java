package ru.bmstu.ORM.Compiler.Analyzer.Lexer;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

public class Message implements Comparable<Message> {
    //private boolean isError;
    private String text;
    private Position pos;

    public Message(Position pos, String text) {
        //this.isError = isError;
        this.text = text;
        this.pos = pos;
    }

    public String getText() {
        return text;
    }

//    public boolean isError() {
//        return isError;
//    }

    @Override
    public int compareTo(Message o) {
        return this.pos.compareTo(o.pos);
    }

    @Override
    public String toString() {
        return text + " at " + pos;
    }
}
