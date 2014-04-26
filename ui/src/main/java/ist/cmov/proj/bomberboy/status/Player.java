package ist.cmov.proj.bomberboy.status;

/**
 * Created by agfrg on 31/03/14.
 */
public class Player {
    public Integer x;
    public Integer y;
    public Boolean bomb;
    public Boolean dead;

    public Player(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        this.bomb = false;
        this.dead = false;
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

    public void toggleBomb() {
        bomb = !bomb;
    }

    public void kill() {
        dead = true;
    }

    public Boolean hasBomb() {
        return bomb;
    }
}
