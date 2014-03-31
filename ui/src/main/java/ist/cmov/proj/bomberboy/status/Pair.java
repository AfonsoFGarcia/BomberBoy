package ist.cmov.proj.bomberboy.status;

/**
 * Created by agfrg on 31/03/14.
 */
public class Pair {
    public Integer x;
    public Integer y;

    public Pair(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public void incrX() {
        x++;
    }

    public void decrX() {
        x--;
    }

    public void incrY() {
        y++;
    }

    public void decrY() {
        y--;
    }
}
