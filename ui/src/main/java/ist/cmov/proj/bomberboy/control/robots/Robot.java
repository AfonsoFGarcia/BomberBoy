package ist.cmov.proj.bomberboy.control.robots;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;
import ist.cmov.proj.bomberboy.utils.SettingsReader;

/**
 * Created by agfrg on 26/04/14.
 */
public class Robot extends Thread implements Controllable {

    private static double SLEEPTIME;
    private static int THRESHOLD = 5;

    private GameStatus status;
    private Integer x;
    private Integer y;
    private Boolean dead;
    private Boolean bomb;
    private Integer id;

    public Robot(GameStatus status, Integer x, Integer y, Integer id) {
        this.status = status;
        this.x = x;
        this.y = y;
        this.id = id;
        this.dead = false;
        this.bomb = false;
    }

    public void increaseScore(int points) {
    }

    public void initializeSettings() {
        SLEEPTIME = (1 / SettingsReader.getSettings().getRobotSpeed()) * 1000;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    private Double getDistance(Player p) {
        return Math.abs(Math.sqrt(Math.pow(p.getX() - getX(), 2) + Math.pow(p.getY() - getY(), 2)));
    }

    private Player getClosestPlayer() {
        Player player = null;
        Double currentDistance = 0d;

        for (Player p : status.getPlayers()) {
            Double cD = getDistance(p);
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
                Thread.sleep((long) SLEEPTIME);
                Player p = getClosestPlayer();
                if (p == null)
                    continue;
                Quadrant closest = Quadrant.getQuadrant(p, this);
                Double cD = getDistance(p);

                //if (cD < THRESHOLD) dropBomb();

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
        interrupt();
    }

    private void killRobot() {
        interrupt();
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

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public boolean move(Movements e) {
        return status.getServerObject().move(e, id);
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

    public void interrupt() {
        super.interrupt();
        dead = true;
    }

    public boolean dead() {
        return dead;
    }
}
