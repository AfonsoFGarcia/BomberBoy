package ist.cmov.proj.bomberboy.control.players;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;

/**
 * Created by agfrg on 31/03/14.
 */
public class Player implements Controllable {
    public Integer x;
    public Integer y;
    public Boolean bomb;
    public Boolean dead;
    private GameStatus status;
    private Integer id;

    public Player(Integer x, Integer y, GameStatus status) {
        this.x = x;
        this.y = y;
        this.bomb = false;
        this.dead = false;
        this.status = status;
    }

    public Player(Player other, GameStatus status) {
        this.x = other.x;
        this.y = other.y;
        this.bomb = false;
        this.dead = false;
        this.status = status;
    }

    public void setID(Integer id) {
        this.id = id;
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

    private void kill() {
        dead = true;
    }

    public boolean hasBomb() {
        return bomb;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void interrupt() {
        kill();
    }

    public boolean move(Movements e) {
        return status.move(e, id);
    }

    public boolean dropBomb() {
        return status.dropBomb(id);
    }
}
