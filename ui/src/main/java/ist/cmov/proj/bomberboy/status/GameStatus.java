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
        robot.setID(rID);
        r.put(rID, robot);
    }

    private void createPlayer() {
        t[1][1] = Types.PERSON;
        p.put(10, new Player(1, 1, this, 10));
    }

    public Player getPlayer() {
        return p.get(10);
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
        r = new HashMap<Integer, Robot>();
        for (Robot robot : robots) {
            registerRobot(robot);
        }
        createPlayer();
    }

    private boolean canMove(Movements e, Controllable c) {
        if (e.equals(Movements.DOWN) && c.getX() < SIZE - 1 && isNotOccupied(e, c)) {
            return true;
        } else if (e.equals(Movements.UP) && c.getX() > 0 && isNotOccupied(e, c)) {
            return true;
        } else if (e.equals(Movements.LEFT) && c.getY() > 0 && isNotOccupied(e, c)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && c.getY() < SIZE - 1 && isNotOccupied(e, c)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNotOccupied(Movements e, Controllable c) {
        if (e.equals(Movements.DOWN) && emptyPosition(c.getX() + 1, c.getY())) {
            return true;
        } else if (e.equals(Movements.UP) && emptyPosition(c.getX() - 1, c.getY())) {
            return true;
        } else if (e.equals(Movements.LEFT) && emptyPosition(c.getX(), c.getY() - 1)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && emptyPosition(c.getX(), c.getY() + 1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean emptyPosition(int x, int y) {
        return t[x][y].equals(Types.NULL);
    }

    public boolean move(Movements e, Integer id) {
        synchronized (lock) {
            Controllable c = null;

            if (Math.floor(id / 10d) == 1) {
                c = p.get(id);
            } else {
                c = r.get(id);
            }

            if (!canMove(e, c)) return false;

            if (t[c.getX()][c.getY()].equals(Types.PERSONANDBOMB)) {
                t[c.getX()][c.getY()] = Types.BOMB;
            } else {
                t[c.getX()][c.getY()] = Types.NULL;
            }

            if (e.equals(Movements.DOWN)) {
                c.incrX();
            } else if (e.equals(Movements.UP)) {
                c.decrX();
            } else if (e.equals(Movements.LEFT)) {
                c.decrY();
            } else {
                c.incrY();
            }

            if (t[c.getX()][c.getY()].equals(Types.BOMB)) {
                t[c.getX()][c.getY()] = Types.PERSONANDBOMB;
            } else {
                t[c.getX()][c.getY()] = Types.PERSON;
            }

            return true;
        }
    }

    public boolean dropBomb(Integer id) {
        synchronized (lock) {
            Controllable c = null;

            if (Math.floor(id / 10d) == 1) {
                c = p.get(id);
            } else {
                c = r.get(id);
            }

            if (!c.hasBomb()) {
                t[c.getX()][c.getY()] = Types.PERSONANDBOMB;
                c.toggleBomb();

                BlowBombTask task = new BlowBombTask(c);
                task.execute(c.getX(), c.getY());

                return true;
            } else {
                return false;
            }
        }
    }

    private class BlowBombTask extends AsyncTask<Integer, Void, Void> {
        private Controllable c;

        public BlowBombTask(Controllable c) {
            this.c = c;
        }
        protected Void doInBackground(Integer... params) {
            try {
                Thread.sleep(TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock) {
                t[params[0]][params[1]] = Types.NULL;
                c.toggleBomb();
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
