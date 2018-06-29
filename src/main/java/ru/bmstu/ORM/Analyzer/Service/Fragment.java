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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Fragment other = (Fragment) obj;

        return this.start.equals(other.start) && this.follow.equals(other.follow);
    }

    public static Fragment dummyCoords() {
        return new Fragment(Position.dummyPosition(), Position.dummyPosition());
    }
}
