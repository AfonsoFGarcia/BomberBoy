package ist.cmov.proj.bomberboy.status;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.control.robots.Robot;
import ist.cmov.proj.bomberboy.ui.BomberView;

public class GameStatus {

    public static int SIZE = 19;
    private static int RANGE = 8;
    protected static int TIME = 5000;
    protected Object lock = new Object();
    private Types[][] t;
    private HashMap<Integer, Player> p;
    private HashMap<Integer, Robot> r;
    BomberView.BomberThread thread;

    public void addBomberThread(BomberView.BomberThread t) {
        thread = t;
    }

    public Types[][] getMap() {
        return t;
    }

    public Collection<Player> getPlayers() {
        return p.values();
    }

    public GameStatus() {
        p = new HashMap<Integer, Player>();
        r = new HashMap<Integer, Robot>();
    }

    private void registerRobot(Robot robot) {
        int rID = 20 + r.size();
        r.put(rID, robot);
    }

    private void createPlayer() {
        t[1][1] = Types.PERSON;
        p.put(10, new Player(1, 1));
    }

    public void beginGame() {
        for (Robot r : this.r.values()) {
            r.start();
        }
    }

    private void setMap(Types[][] types) {
        t = new Types[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            t[i] = Arrays.copyOf(types[i], types[i].length);
        }
    }

    public void initializeGameStatus(Types[][] types, ArrayList<Robot> robots) {
        setMap(types);
        for (Robot robot : robots) {
            registerRobot(robot);
        }
        createPlayer();
    }

    private boolean canMove(Movements e) {
        if (e.equals(Movements.DOWN) && p.get(10).x < SIZE - 1 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.UP) && p.get(10).x > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.LEFT) && p.get(10).y > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && p.get(10).y < SIZE - 1 && isNotOccupied(e)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNotOccupied(Movements e) {
        if (e.equals(Movements.DOWN) && emptyPosition(p.get(10).x + 1, p.get(10).y)) {
            return true;
        } else if (e.equals(Movements.UP) && emptyPosition(p.get(10).x - 1, p.get(10).y)) {
            return true;
        } else if (e.equals(Movements.LEFT) && emptyPosition(p.get(10).x, p.get(10).y - 1)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && emptyPosition(p.get(10).x, p.get(10).y + 1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean emptyPosition(int x, int y) {
        return t[x][y].equals(Types.NULL);
    }

    public boolean move(Movements e) {
        synchronized (lock) {
            if (!canMove(e)) return false;

            if (t[p.get(10).x][p.get(10).y].equals(Types.PERSONANDBOMB)) {
                t[p.get(10).x][p.get(10).y] = Types.BOMB;
            } else {
                t[p.get(10).x][p.get(10).y] = Types.NULL;
            }

            if (e.equals(Movements.DOWN)) {
                p.get(10).incrX();
            } else if (e.equals(Movements.UP)) {
                p.get(10).decrX();
            } else if (e.equals(Movements.LEFT)) {
                p.get(10).decrY();
            } else {
                p.get(10).incrY();
            }

            if (t[p.get(10).x][p.get(10).y].equals(Types.BOMB)) {
                t[p.get(10).x][p.get(10).y] = Types.PERSONANDBOMB;
            } else {
                t[p.get(10).x][p.get(10).y] = Types.PERSON;
            }

            return true;
        }
    }

    public boolean dropBomb() {
        synchronized (lock) {
            if (!p.get(10).hasBomb()) {
                t[p.get(10).x][p.get(10).y] = Types.PERSONANDBOMB;
                p.get(10).toggleBomb();

                BlowBombTask task = new BlowBombTask();
                task.execute(p.get(10).x, p.get(10).y);

                return true;
            } else {
                return false;
            }
        }
    }

    private class BlowBombTask extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {
            try {
                Thread.sleep(TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock) {
                t[params[0]][params[1]] = Types.NULL;
                p.get(10).toggleBomb();
                if (cleanTab(params[0], params[1])) {
                    thread.smellyDied();
                }
            }

            return null;
        }

        private boolean killControllables(int x, int y) {
            ArrayList<Controllable> controllables = new ArrayList<Controllable>();
            controllables.addAll(r.values());
            controllables.addAll(p.values());

            boolean returnValue = false;

            for (Controllable c : controllables) {
                if (c.getX() == x && c.getY() == y) {
                    if (c instanceof Player) {
                        returnValue = true;
                    }
                    c.interrupt();
                }
            }

            return returnValue;
        }

        private boolean cleanTab(int x, int y) {
            boolean returnValue = false;
            for (int i = 0; i < RANGE; i++) {
                if (y + i < SIZE && !t[x][y + i].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x, y + i);
                    t[x][y + i] = Types.NULL;
                } else {
                    break;
                }
            }
            for (int i = 0; i < RANGE; i++) {
                if (x + i < SIZE && !t[x + i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x + i, y);
                    t[x + i][y] = Types.NULL;
                } else {
                    break;
                }
            }
            for (int i = 0; i < RANGE; i++) {
                if (y - i >= 0 && !t[x][y - i].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x, y - i);
                    t[x][y - i] = Types.NULL;
                } else {
                    break;
                }
            }
            for (int i = 0; i < RANGE; i++) {
                if (x - i >= 0 && !t[x - i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x - i, y);
                    t[x - i][y] = Types.NULL;
                } else {
                    break;
                }
            }
            return returnValue;
        }
    }
}
