package ist.cmov.proj.bomberboy.control.players;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;
import ist.cmov.proj.bomberboy.ui.Main;

/**
 * Created by agfrg on 31/03/14.
 */
public class Player implements Controllable {
    public Integer x;
    public Integer y;
    public Boolean bomb;
    public Boolean dead;
    private String name;
    private String url;
    private GameStatus status;
    private Integer id;
    private Integer points;
    private Main mainActivity;

    public Player(Integer id, String name, Integer x, Integer y, GameStatus status, Main mainActivity) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.status = status;
        this.mainActivity = mainActivity;
        this.bomb = false;
        this.dead = false;
        this.points = 0;
    }

    public Player(Integer x, Integer y, Integer id, GameStatus status, Main mainActivity) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.bomb = false;
        this.dead = false;
        this.status = status;
        this.points = 0;
        this.mainActivity = mainActivity;
    }

    public void increaseScore(int points) {
        this.points += points;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setPoints(getScore().toString());
            }
        });
    }

    public Integer getScore() {
        return points;
    }

    public Player(Player other, GameStatus status) {
        this(other.x, other.y, other.id, status, other.mainActivity);
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public int getID() {
        return id;
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
