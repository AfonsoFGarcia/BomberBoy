package ist.cmov.proj.bomberboy.control.robots;

import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.status.Movements;

/**
 * Created by agfrg on 26/04/14.
 */
public enum Quadrant {
    TOPLEFT(Movements.UP, Movements.LEFT, Movements.RIGHT, Movements.DOWN),
    TOPRIGHT(Movements.UP, Movements.RIGHT, Movements.LEFT, Movements.DOWN),
    BOTTOMLEFT(Movements.DOWN, Movements.LEFT, Movements.RIGHT, Movements.UP),
    BOTTOMRIGHT(Movements.DOWN, Movements.RIGHT, Movements.LEFT, Movements.UP);

    private final Movements moveOne;
    private final Movements moveTwo;
    private final Movements moveThree;
    private final Movements moveFour;

    Quadrant(Movements moveOne, Movements moveTwo, Movements moveThree, Movements moveFour) {
        this.moveOne = moveOne;
        this.moveTwo = moveTwo;
        this.moveThree = moveThree;
        this.moveFour = moveFour;
    }

    public Movements getMoveOne() {
        return moveOne;
    }

    public Movements getMoveTwo() {
        return moveTwo;
    }

    public Movements getMoveThree() {
        return moveTwo;
    }

    public Movements getMoveFour() {
        return moveTwo;
    }

    public static Quadrant getQuadrant(Player p, Robot r) {
        if (p.getX() <= r.getX()) {
            if (p.getY() <= r.getY()) {
                return TOPLEFT;
            } else {
                return TOPRIGHT;
            }
        } else {
            if (p.getY() <= r.getY()) {
                return BOTTOMLEFT;
            } else {
                return BOTTOMRIGHT;
            }
        }
    }
}
