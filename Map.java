package assignments.Ex2;

import java.io.Serializable;
import java.util.ArrayDeque;

public class Map implements Map2D, Serializable {

    private int[][] _map; // [x][y]

    public Map(int w, int h, int v) { init(w, h, v); }
    public Map(int size) { this(size, size, 0); }
    public Map(int[][] data) { init(data); }

    @Override
    public void init(int w, int h, int v) {
        if (w <= 0 || h <= 0) throw new RuntimeException("Illegal size");
        _map = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                _map[x][y] = v;
            }
        }
    }

    @Override
    public void init(int[][] arr) {
        if (arr == null || arr.length == 0 || arr[0] == null || arr[0].length == 0)
            throw new RuntimeException("Illegal array");

        int w = arr.length;
        int h = arr[0].length;

        for (int x = 0; x < w; x++) {
            if (arr[x] == null || arr[x].length != h)
                throw new RuntimeException("Ragged/illegal 2D array");
        }

        _map = deepCopy(arr);
    }

    @Override
    public int[][] getMap() {
        return deepCopy(_map);
    }

    @Override
    public int getWidth() {
        return _map.length;
    }

    @Override
    public int getHeight() {
        return _map[0].length;
    }

    @Override
    public int getPixel(int x, int y) {
        checkInside(x, y);
        return _map[x][y];
    }

    @Override
    public int getPixel(Pixel2D p) {
        if (p == null) throw new RuntimeException("p is null");
        return getPixel(p.getX(), p.getY());
    }

    @Override
    public void setPixel(int x, int y, int v) {
        checkInside(x, y);
        _map[x][y] = v;
    }

    @Override
    public void setPixel(Pixel2D p, int v) {
        if (p == null) throw new RuntimeException("p is null");
        setPixel(p.getX(), p.getY(), v);
    }

    @Override
    public boolean isInside(Pixel2D p) {
        if (p == null) return false;
        int x = p.getX(), y = p.getY();
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    @Override
    public boolean sameDimensions(Map2D p) {
        if (p == null) return false;
        return getWidth() == p.getWidth() && getHeight() == p.getHeight();
    }

    @Override
    public void addMap2D(Map2D p) {
        if (p == null) throw new RuntimeException("p is null");
        if (!sameDimensions(p)) return; // interface says: else do nothing
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                _map[x][y] += p.getPixel(x, y);
            }
        }
    }

    @Override
    public void mul(double scalar) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                _map[x][y] = (int) (_map[x][y] * scalar);
            }
        }
    }

    @Override
    public void rescale(double sx, double sy) {
        if (sx <= 0 || sy <= 0) throw new RuntimeException("Illegal scale");
        int oldW = getWidth(), oldH = getHeight();
        int newW = Math.max(1, (int) Math.round(oldW * sx));
        int newH = Math.max(1, (int) Math.round(oldH * sy));

        int[][] n = new int[newW][newH];

        for (int x = 0; x < newW; x++) {
            for (int y = 0; y < newH; y++) {
                int ox = (int) Math.floor(x / sx);
                int oy = (int) Math.floor(y / sy);
                ox = clamp(ox, 0, oldW - 1);
                oy = clamp(oy, 0, oldH - 1);
                n[x][y] = _map[ox][oy];
            }
        }
        _map = n;
    }

    @Override
    public void drawCircle(Pixel2D center, double rad, int color) {
        if (center == null) throw new RuntimeException("center is null");
        if (rad < 0) throw new RuntimeException("rad < 0");
        int cx = center.getX(), cy = center.getY();

        int xmin = (int) Math.floor(cx - rad);
        int xmax = (int) Math.ceil(cx + rad);
        int ymin = (int) Math.floor(cy - rad);
        int ymax = (int) Math.ceil(cy + rad);

        double rr = rad * rad;

        for (int x = xmin; x <= xmax; x++) {
            for (int y = ymin; y <= ymax; y++) {
                if (inside(x, y)) {
                    double dx = x - cx;
                    double dy = y - cy;
                    if (dx * dx + dy * dy <= rr) {
                        _map[x][y] = color;
                    }
                }
            }
        }
    }

    // Implements EXACT spec in Map2D comment (rounding along dominant axis)
    @Override
    public void drawLine(Pixel2D p1, Pixel2D p2, int color) {
        if (p1 == null || p2 == null) throw new RuntimeException("null pixel");
        if (!isInside(p1) || !isInside(p2)) throw new RuntimeException("pixel out of bounds");

        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();

        if (x1 == x2 && y1 == y2) {
            _map[x1][y1] = color;
            return;
        }

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        // dominant axis X
        if (dx >= dy) {
            // ensure x1 <= x2 (rule: else draw p2->p1)
            if (x1 > x2) {
                int tx = x1; x1 = x2; x2 = tx;
                int ty = y1; y1 = y2; y2 = ty;
            }
            double slope = (x2 == x1) ? 0 : (double) (y2 - y1) / (double) (x2 - x1);
            for (int x = x1; x <= x2; x++) {
                double fy = y1 + slope * (x - x1);
                int y = (int) Math.round(fy);
                if (inside(x, y)) _map[x][y] = color;
            }
        } else {
            // dominant axis Y
            if (y1 > y2) {
                int tx = x1; x1 = x2; x2 = tx;
                int ty = y1; y1 = y2; y2 = ty;
            }
            double slope = (y2 == y1) ? 0 : (double) (x2 - x1) / (double) (y2 - y1);
            for (int y = y1; y <= y2; y++) {
                double fx = x1 + slope * (y - y1);
                int x = (int) Math.round(fx);
                if (inside(x, y)) _map[x][y] = color;
            }
        }
    }

    @Override
    public void drawRect(Pixel2D p1, Pixel2D p2, int color) {
        if (p1 == null || p2 == null) throw new RuntimeException("null pixel");
        int xMin = Math.min(p1.getX(), p2.getX());
        int xMax = Math.max(p1.getX(), p2.getX());
        int yMin = Math.min(p1.getY(), p2.getY());
        int yMax = Math.max(p1.getY(), p2.getY());

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                if (inside(x, y)) _map[x][y] = color;
            }
        }
    }

    @Override
    public boolean equals(Object ob) {
        if (this == ob) return true;
        if (!(ob instanceof Map2D)) return false;
        Map2D o = (Map2D) ob;

        if (!sameDimensions(o)) return false;

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (_map[x][y] != o.getPixel(x, y)) return false;
            }
        }
        return true;
    }

    @Override
    public int fill(Pixel2D xy, int new_v, boolean cyclic) {
        if (xy == null) throw new RuntimeException("xy is null");
        if (!isInside(xy)) throw new RuntimeException("xy out of bounds");

        int sx = xy.getX(), sy = xy.getY();
        int old = _map[sx][sy];
        if (old == new_v) return 0;

        boolean[][] vis = new boolean[getWidth()][getHeight()];
        ArrayDeque<Index2D> q = new ArrayDeque<>();
        q.add(new Index2D(sx, sy));
        vis[sx][sy] = true;

        int count = 0;

        while (!q.isEmpty()) {
            Index2D cur = q.removeFirst();
            int x = cur.getX(), y = cur.getY();

            if (_map[x][y] != old) continue;

            _map[x][y] = new_v;
            count++;

            addNeighborIf(q, vis, x - 1, y, old, cyclic);
            addNeighborIf(q, vis, x + 1, y, old, cyclic);
            addNeighborIf(q, vis, x, y - 1, old, cyclic);
            addNeighborIf(q, vis, x, y + 1, old, cyclic);
        }

        return count;
    }

    @Override
    public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor, boolean cyclic) {
        // not needed for your shown tests; implement later for full Ex2
        return null;
    }

    @Override
    public Map2D allDistance(Pixel2D start, int obsColor, boolean cyclic) {
        // not needed for your shown tests; implement later for full Ex2
        return null;
    }

    // ---------- private helpers ----------

    private static int[][] deepCopy(int[][] a) {
        if (a == null) return null;
        int w = a.length;
        int h = a[0].length;
        int[][] b = new int[w][h];
        for (int x = 0; x < w; x++) {
            System.arraycopy(a[x], 0, b[x], 0, h);
        }
        return b;
    }

    private boolean inside(int x, int y) {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    private void checkInside(int x, int y) {
        if (!inside(x, y)) throw new RuntimeException("Out of bounds: " + x + "," + y);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private void addNeighborIf(ArrayDeque<Index2D> q, boolean[][] vis, int nx, int ny, int old, boolean cyclic) {
        int w = getWidth(), h = getHeight();

        if (cyclic) {
            nx = (nx % w + w) % w;
            ny = (ny % h + h) % h;
        } else {
            if (!inside(nx, ny)) return;
        }

        if (!vis[nx][ny] && _map[nx][ny] == old) {
            vis[nx][ny] = true;
            q.add(new Index2D(nx, ny));
        }
    }
}
