package ist.cmov.proj.bomberboy.status;

import android.graphics.Canvas;

public class GameStatus {

    public static int SIZE = 19;
    private Types[][] t;
    private Pair p = null;

    public Types[][] getMap() {
        return t;
    }

    private void createPlayer() {
        t[1][1] = Types.PERSON;
        p = new Pair(1, 1);
    }

    public GameStatus(Types[][] types) {
        t = types;
        createPlayer();
    }

    private boolean canMove(Movements e) {
        if (e.equals(Movements.DOWN) && p.x < SIZE - 1 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.UP) && p.x > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.LEFT) && p.y > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && p.y < SIZE - 1 && isNotOccupied(e)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNotOccupied(Movements e) {
        if (e.equals(Movements.DOWN) && t[p.x + 1][p.y].equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.UP) && t[p.x - 1][p.y].equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.LEFT) && t[p.x][p.y - 1].equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && t[p.x][p.y + 1].equals(Types.NULL)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean move(Movements e, Canvas canvas) {
        if (!canMove(e)) return false;
        t[p.x][p.y] = Types.NULL;
        if (e.equals(Movements.DOWN)) {
            p.incrX();
        } else if (e.equals(Movements.UP)) {
            p.decrX();
        } else if (e.equals(Movements.LEFT)) {
            p.decrY();
        } else {
            p.incrY();
        }
        t[p.x][p.y] = Types.PERSON;
        return true;
    }
}
