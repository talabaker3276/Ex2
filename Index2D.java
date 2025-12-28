package assignments.Ex2;

public class Index2D implements Pixel2D {
    private final int _x;
    private final int _y;

    public Index2D(int x, int y) {
        _x = x;
        _y = y;
    }

    public Index2D(Pixel2D other) {
        if (other == null) throw new RuntimeException("other is null");
        _x = other.getX();
        _y = other.getY();
    }

    @Override
    public int getX() {
        return _x;
    }

    @Override
    public int getY() {
        return _y;
    }

    @Override
    public double distance2D(Pixel2D p2) {
        if (p2 == null) throw new RuntimeException("p2 is null");
        int dx = _x - p2.getX();
        int dy = _y - p2.getY();
        return Math.sqrt((double)dx * dx + (double)dy * dy);
    }

    @Override
    public String toString() {
        return _x + "," + _y;
    }

    @Override
    public boolean equals(Object p) {
        if (this == p) return true;
        if (!(p instanceof Pixel2D)) return false;
        Pixel2D o = (Pixel2D) p;
        return _x == o.getX() && _y == o.getY();
    }

    @Override
    public int hashCode() {
        return 31 * _x + _y;
    }
}
