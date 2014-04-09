package ist.cmov.proj.bomberboy.status;

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
        if (e.equals(Movements.DOWN) && emptyPosition(p.x + 1, p.y)) {
            return true;
        } else if (e.equals(Movements.UP) && emptyPosition(p.x - 1, p.y)) {
            return true;
        } else if (e.equals(Movements.LEFT) && emptyPosition(p.x, p.y - 1)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && emptyPosition(p.x, p.y + 1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean emptyPosition(int x, int y) {
        return t[x][y].equals(Types.NULL);
    }

    public boolean move(Movements e) {
        if (!canMove(e)) return false;

        if (t[p.x][p.y].equals(Types.PERSONANDBOMB)) {
            t[p.x][p.y] = Types.BOMB;
        } else {
            t[p.x][p.y] = Types.NULL;
        }

        if (e.equals(Movements.DOWN)) {
            p.incrX();
        } else if (e.equals(Movements.UP)) {
            p.decrX();
        } else if (e.equals(Movements.LEFT)) {
            p.decrY();
        } else {
            p.incrY();
        }

        if (t[p.x][p.y].equals(Types.BOMB)) {
            t[p.x][p.y] = Types.PERSONANDBOMB;
        } else {
            t[p.x][p.y] = Types.PERSON;
        }


        return true;
    }

    public boolean dropBomb() {
        t[p.x][p.y] = Types.PERSONANDBOMB;
        return true;
    }
}
