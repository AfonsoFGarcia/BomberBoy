package ist.cmov.proj.bomberboy.status;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.control.robots.Robot;
import ist.cmov.proj.bomberboy.ui.BomberView;
import ist.cmov.proj.bomberboy.ui.Main;
import ist.cmov.proj.bomberboy.utils.GameSettings;
import ist.cmov.proj.bomberboy.utils.SettingsReader;
import ist.cmov.proj.bomberboy.server.*;

public class GameStatus {

    public static int SIZE = 19;
    protected static boolean GAMEOVER = false;
    protected static int RANGE;
    protected static int TIMETOBLOW;
    protected static int TIMEOFBLOW;

    protected Object lock = new Object();
    private Player me;
    private Types[][] t;
    private HashMap<Integer, Player> p;
    private HashMap<Integer, Robot> r;
    private HashMap<Integer, Integer> serverIDs; // map with <localID, serverID>
    private Stack<Player> playerStack;
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
        serverIDs = new HashMap<Integer, Integer>();
    }

    public void initializeSettings() {
        RANGE = SettingsReader.getSettings().getExplosionRange();
        TIMETOBLOW = SettingsReader.getSettings().getExplosionTimeout() * 1000;
        TIMEOFBLOW = SettingsReader.getSettings().getExplosionDuration() * 1000;
        for (Robot robot : r.values()) {
            robot.initializeSettings();
        }
    }

    private void registerRobot(Robot robot) {
        int rID = 20 + r.size();
        robot.setID(rID);
        r.put(rID, robot);
    }

    private void createPlayer(Player player) {
        t[player.getX()][player.getY()] = Types.PERSON;
        int pID = 10 + p.size();
        player.setID(pID);
        p.put(pID, player);
    }

    public Player getPlayer() {
        Player player = new Player(playerStack.pop(), this);
        createPlayer(player);
        me = player;
        return player;
    }

    public void endGame() {
        for (Robot r : this.r.values()) {
            r.stopRobot();
        }
    }

    public void beginGame() {
        HashMap<Integer, Robot> rNews = new HashMap<Integer, Robot>();
        for (Robot r : this.r.values()) {
            Robot rNew = new Robot(this, r.getX(), r.getY());
            rNew.setID(r.getID());
            rNews.put(r.getID(), rNew);
            rNew.start();
        }
        r = rNews;
    }

    private void setMap(Types[][] types) {
        t = new Types[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            t[i] = Arrays.copyOf(types[i], types[i].length);
        }
    }

    public void initializeGameStatus(GameSettings settings) {
        setMap(settings.getMap());
        r = new HashMap<Integer, Robot>();
        p = new HashMap<Integer, Player>();
        for (Robot robot : settings.getRobots()) {
            registerRobot(robot);
        }
        playerStack = (Stack<Player>) settings.getPlayers().clone();
        GAMEOVER = false;
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
        return t[x][y].equals(Types.NULL) || t[x][y].equals(Types.EXPLOSION) || t[x][y].equals(Types.EXPLOSIONANDBOMB);
    }

    private boolean diedInNuclearFallout(Controllable c) {
        if (t[c.getX()][c.getY()].equals(Types.EXPLOSION)) {
            c.interrupt();
            return true;
        }
        return false;
    }

    private void moveClean(Player c) {
        if (t[c.getX()][c.getY()].equals(Types.PERSONANDBOMB)) {
            t[c.getX()][c.getY()] = Types.BOMB;
        } else {
            t[c.getX()][c.getY()] = Types.NULL;
        }
    }

    private void moveClean(Robot c) {
        if (t[c.getX()][c.getY()].equals(Types.ROBOTANDBOMB)) {
            t[c.getX()][c.getY()] = Types.BOMB;
        } else {
            t[c.getX()][c.getY()] = Types.NULL;
        }
    }

    private void movePlace(Player c) {
        if (t[c.getX()][c.getY()].equals(Types.BOMB)) {
            t[c.getX()][c.getY()] = Types.PERSONANDBOMB;
        } else {
            t[c.getX()][c.getY()] = Types.PERSON;
        }
    }

    private void movePlace(Robot c) {
        if (t[c.getX()][c.getY()].equals(Types.BOMB)) {
            t[c.getX()][c.getY()] = Types.ROBOTANDBOMB;
        } else {
            t[c.getX()][c.getY()] = Types.ROBOT;
        }
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

            if (c instanceof Player) {
                moveClean((Player) c);
            } else {
                moveClean((Robot) c);
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

            if (diedInNuclearFallout(c) && c instanceof Player) {
                thread.smellyDied();
                return true;
            }

            if (c instanceof Player) {
                movePlace((Player) c);
                // update the server with the new position
                int localID = c.getID();
                String msg = "move " + serverIDs.get(localID) + " " + c.getX() + " " + c.getY();
                new ClientConnectorTask().execute(msg);
            } else {
                movePlace((Robot) c);
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
                if (c instanceof Player)
                    t[c.getX()][c.getY()] = Types.PERSONANDBOMB;
                else
                    t[c.getX()][c.getY()] = Types.ROBOTANDBOMB;

                c.toggleBomb();

                BlowBombTask task = new BlowBombTask(c);
                task.execute(c.getX(), c.getY());

                return true;
            } else {
                return false;
            }
        }
    }

    // register the player on the server
    public void register(String name, int x, int y) {
        me.setName(name);
        String ipAddr = NetworkUtils.getIPAddress();
        String msg = "register " + name + " " + ipAddr + " " + x + " " + y;
        new ClientConnectorTask().execute(msg);
    }

    // add a new player to the game
    public void addPlayer(String id, String name) {
/*
        Types playerType;
        if (trashman.equals("1"))
            playerType = Types.SMELLY1;

        if (trashman.equals("2"))
            playerType = Types.SMELLY2;

        if (trashman.equals("3"))
            playerType = Types.SMELLY3;*/

        // TODO: there should be a method to add the Type of the player and set the name
        int localID = 0;
        if(!me.getName().equals(name)) {
            Player player = getPlayer();
            player.setName(name);
            localID = player.getID();
        } else {
            localID = me.getID();
        }
        serverIDs.put(localID, Integer.parseInt(id));
    }

    public void moveAnotherSmelly(Integer id, String direction) {
        int localID = 0;
        for(Map.Entry<Integer, Integer> entry : serverIDs.entrySet()) {
            if(id.equals(entry.getValue())) {
                localID = entry.getKey();
            }
        }

        Player OpSmelly = p.get(localID);

        moveClean(OpSmelly);

        if (direction.equals("down")) {
            OpSmelly.incrX();
        } else if (direction.equals("up")) {
            OpSmelly.decrX();
        } else if (direction.equals("left")) {
            OpSmelly.decrY();
        } else {
            OpSmelly.incrY();
        }
        movePlace(OpSmelly);
        Main.game.signalRedraw();
    }

    private class BlowBombTask extends AsyncTask<Integer, Void, Void> {
        private Controllable controllable;

        public BlowBombTask(Controllable c) {
            this.controllable = c;
        }

        protected Void doInBackground(Integer... params) {
            try {
                Thread.sleep(TIMETOBLOW);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock) {
                t[params[0]][params[1]] = Types.EXPLOSION;
                controllable.toggleBomb();
                if (cleanTab(params[0], params[1])) {
                    thread.smellyDied();
                    GAMEOVER = true;
                }
            }

            if (!GAMEOVER) {
                try {
                    Thread.sleep(TIMEOFBLOW);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            synchronized (lock) {
                t[params[0]][params[1]] = Types.NULL;
                cleanExplosion(params[0], params[1]);
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

                    if (c instanceof Player && c.getID() == controllable.getID()) {
                        returnValue = true;
                    } else if (c instanceof Robot) {
                        controllable.increaseScore(SettingsReader.getSettings().getPointsPerRobot());
                    } else {
                        controllable.increaseScore(SettingsReader.getSettings().getPointsPerPlayer());
                    }
                    c.interrupt();
                }
            }

            return returnValue;
        }

        private void cleanExplosion(int x, int y) {
            for (int i = 0; i < RANGE; i++) {
                if (y + i < SIZE && !t[x][y + i].equals(Types.WALL)) {
                    cleanExplosionAux(x, y + i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < RANGE; i++) {
                if (x + i < SIZE && !t[x + i][y].equals(Types.WALL)) {
                    cleanExplosionAux(x + i, y);
                } else {
                    break;
                }
            }
            for (int i = 1; i < RANGE; i++) {
                if (y - i >= 0 && !t[x][y - i].equals(Types.WALL)) {
                    cleanExplosionAux(x, y - i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < RANGE; i++) {
                if (x - i >= 0 && !t[x - i][y].equals(Types.WALL)) {
                    cleanExplosionAux(x - i, y);
                } else {
                    break;
                }
            }
        }

        private void setExplosion(int x, int y) {
            if (t[x][y].equals(Types.BOMB)) {
                t[x][y] = Types.EXPLOSIONANDBOMB;
            } else {
                t[x][y] = Types.EXPLOSION;
            }
        }

        private void cleanExplosionAux(int x, int y) {
            if (t[x][y].equals(Types.EXPLOSIONANDBOMB)) {
                t[x][y] = Types.BOMB;
            } else {
                t[x][y] = Types.NULL;
            }
        }

        private boolean cleanTab(int x, int y) {
            boolean returnValue = false;

            returnValue = returnValue || killControllables(x, y);
            t[x][y] = Types.EXPLOSION;

            for (int i = 1; i < RANGE; i++) {
                if (y + i < SIZE && !t[x][y + i].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x, y + i);
                    setExplosion(x, y + i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < RANGE; i++) {
                if (x + i < SIZE && !t[x + i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x + i, y);
                    setExplosion(x + i, y);
                } else {
                    break;
                }
            }
            for (int i = 1; i < RANGE; i++) {
                if (y - i >= 0 && !t[x][y - i].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x, y - i);
                    setExplosion(x, y - i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < RANGE; i++) {
                if (x - i >= 0 && !t[x - i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x - i, y);
                    setExplosion(x - i, y);
                } else {
                    break;
                }
            }
            return returnValue;
        }
    }
}
