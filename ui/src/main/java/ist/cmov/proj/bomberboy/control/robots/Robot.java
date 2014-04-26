package ist.cmov.proj.bomberboy.control.robots;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.status.GameStatus;

/**
 * Created by agfrg on 26/04/14.
 */
public class Robot extends Thread implements Controllable {

    private static int SLEEPTIME = 1000;

    private GameStatus status;
    private Integer x;
    private Integer y;
    private Boolean dead;
    private Boolean bomb;

    public Robot(GameStatus status, Integer x, Integer y) {
        this.status = status;
        this.x = x;
        this.y = y;
        this.dead = false;
        this.bomb = false;
    }

    @Override
    public void run() {
        while (!dead) {
            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                killRobot();
            }
        }
    }

    private void killRobot() {
        this.dead = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
