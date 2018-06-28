package ru.bmstu.ORM.Analyzer.Service;

public class Fragment {
    private Position start, follow;

    public Fragment(Position start, Position follow) {
        this.start = start;
        this.follow = follow;
    }

    @Override
    public String toString() {
        return start.toString() + "-" + follow.toString();
    }

    public void setStart(Position start) {
        this.start = start;
    }

    public void setFollow(Position follow) {
        this.follow = follow;
    }

    public Position getStart() {
        return start;
    }

    public Position getFollow() {
        return follow;
    }
}
