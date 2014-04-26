package ist.cmov.proj.bomberboy.control.robots;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;

/**
 * Created by agfrg on 26/04/14.
 */
public class Robot extends Thread implements Controllable {

    private static int SLEEPTIME = 1000;
    private static int THRESHOLD = 5;

    private GameStatus status;
    private Integer x;
    private Integer y;
    private Boolean dead;
    private Boolean bomb;
    private Integer id;

    public Robot(GameStatus status, Integer x, Integer y) {
        this.status = status;
        this.x = x;
        this.y = y;
        this.dead = false;
        this.bomb = false;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public Integer getID() {
        return id;
    }

    private Player getClosestPlayer() {
        Player player = null;
        Double currentDistance = 0d;

        for (Player p : status.getPlayers()) {
            Double cD = Math.abs(Math.sqrt(Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2)) - Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2)));
            if (player == null) {
                player = p;
                currentDistance = cD;
            } else if (cD < currentDistance) {
                player = p;
                currentDistance = cD;
            }
        }

        return player;
    }

    @Override
    public void run() {
        while (!dead) {
            try {
                Thread.sleep(SLEEPTIME);
                Player p = getClosestPlayer();
                Quadrant closest = Quadrant.getQuadrant(p, this);
                Double cD = Math.abs(Math.sqrt(Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2)) - Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2)));

                if (cD < THRESHOLD) dropBomb();

                if (move(closest.getMoveOne())) {
                } else if (move(closest.getMoveTwo())) {
                } else if (move(closest.getMoveThree())) {
                } else {
                    move(closest.getMoveFour());
                }
            } catch (InterruptedException e) {
                killRobot();
            }
        }
    }

    public void stopRobot() {
        dead = true;
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

    public boolean move(Movements e) {
        return status.move(e, id);
    }

    public boolean dropBomb() {
        return status.dropBomb(id);
    }

    public boolean hasBomb() {
        return bomb;
    }

    public void toggleBomb() {
        bomb = !bomb;
    }
}
