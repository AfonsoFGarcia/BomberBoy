package ist.cmov.proj.bomberboy.control.robots;

import ist.cmov.proj.bomberboy.control.players.Player;

/**
 * Created by agfrg on 26/04/14.
 */
public enum Quadrant {
    TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT;

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
