package ru.bmstu.ORM.Analyzer.Service;

public class Position implements Comparable<Position>, Cloneable {
    private String text;
    private Integer line, pos, index;

    public Position(String text) {
        this.text = text;
        line = pos = 1;
        index = 0;
    }

    public char getChar() {
        return (index == text.length()) ? (char) 0xFFFFFFFF : Character.toLowerCase(text.charAt(index));
    }

    public boolean isNewLine() {
        if (index == text.length())
            return true;

        if (text.charAt(index) == '\r' && index + 1 < text.length())
            return text.charAt(index + 1) == '\n';

        return text.charAt(index) == '\n';
    }

    public boolean isWhiteSpace() {
        return index != text.length() && Character.isWhitespace(text.charAt(index));
    }

    public boolean isLetter() {
        return index != text.length() && Character.isLetter(text.charAt(index));
    }

    public boolean isDigit() {
        return index != text.length() && Character.isDigit(text.charAt(index));
    }

    public boolean isLetterOrDigit() {
        return index != text.length() && Character.isLetterOrDigit(text.charAt(index));
    }

    public boolean isSpecial() {
        return index != text.length() && (
                text.charAt(index) == ';' ||
                text.charAt(index) == '(' ||
                text.charAt(index) == ')' ||
                text.charAt(index) == '+' ||
                text.charAt(index) == '-' ||
                text.charAt(index) == '*' ||
                text.charAt(index) == '/' ||
                text.charAt(index) == '<' ||
                text.charAt(index) == '>' ||
                text.charAt(index) == '=' ||
                text.charAt(index) == '!'
                );
    }

    public void nextCp() {
        if (index < text.length()) {
            if (isNewLine()) {
                if (text.charAt(index) == '\r')
                    index++;
                line++;
                pos = 1;
            } else {
                if (Character.isHighSurrogate(text.charAt(index)))
                    index++;
                pos++;
            }
            index++;
        }
    }

    public boolean isEOF() {
        return this.getChar() == (char) 0xFFFFFFFF;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(Position other) {
        return index.compareTo(other.index);
    }

    @Override
    public String toString() {
        return " ( " + line + " , " + pos + " ) ";
    }
}
