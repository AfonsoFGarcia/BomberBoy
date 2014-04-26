package ist.cmov.proj.bomberboy.control;

import ist.cmov.proj.bomberboy.status.Movements;

/**
 * Created by agfrg on 26/04/14.
 */
public interface Controllable {

    public int getX();

    public int getY();

    public int getID();

    public void interrupt();

    public boolean move(Movements e);

    public boolean dropBomb();

    public boolean hasBomb();

    public void toggleBomb();

    public void incrX();

    public void decrX();

    public void incrY();

    public void decrY();

    public void increaseScore(int points);
}
