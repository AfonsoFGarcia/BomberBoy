package ist.cmov.proj.bomberboy.status;

import android.net.wifi.p2p.WifiP2pInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.control.robots.Robot;
import ist.cmov.proj.bomberboy.ui.BomberView;
import ist.cmov.proj.bomberboy.ui.Main;
import ist.cmov.proj.bomberboy.utils.GameSettings;
import ist.cmov.proj.bomberboy.utils.SettingsReader;
import ist.cmov.proj.bomberboy.server.*;
import ist.cmov.proj.bomberboy.wifidirect.Server;

public class GameStatus {

    public static int SIZE = 19;
    public static boolean GAMESTARTED = false;
    public static boolean SERVER_MODE = true; // by default so the player can play alone
    public static WifiP2pInfo info;
    protected static boolean GAMEOVER = false;
    protected static int RANGE;
    protected static int TIMETOBLOW;
    protected static int TIMEOFBLOW;
    protected static ArrayList<Timer> timers = new ArrayList<Timer>();

    protected Object lock = new Object();
    private Server server;
    private Main main;
    private Player me;
    private Types[][] t;
    private HashMap<Integer, Player> p;
    private HashMap<Integer, Robot> r;
    private Stack<Player> playerStack;
    BomberView.BomberThread thread;

    public void addBomberThread(BomberView.BomberThread t) {
        thread = t;
    }

    public Types[][] getMap() {
        return t;
    }

    public Server getServerObject() {
        if (SERVER_MODE)
            return server;

        return null;
    }

    public void updatePlayers(HashMap<Integer, Player> players) {
        p = players;
    }

    public Collection<Player> getPlayers() {
        return p.values();
    }

    public Player getMe() {
        return me;
    }

    public GameStatus() {
        p = new HashMap<Integer, Player>();
        r = new HashMap<Integer, Robot>();
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
        int rID = 10 + r.size();
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

    protected void cancelTimers() {
        for (Timer timer : timers) {
            timer.cancel();
            timer.purge();
        }
    }

    public void endGame() {
        cancelTimers();
        for (Robot r : this.r.values()) {
            r.stopRobot();
        }
    }

    public void beginGame() {
        HashMap<Integer, Robot> rNews = new HashMap<Integer, Robot>();
        for (Robot r : this.r.values()) {
            Robot rNew = new Robot(this, r.getX(), r.getY(), r.getID());
            rNew.setID(r.getID());
            rNews.put(r.getID(), rNew);
        }
        r = rNews;

        final Timer t = new Timer();
        timers.add(t);

        class EndGame extends TimerTask {
            public void run() {
                endGame();
                thread.gameEnds();
            }
        }

        t.schedule(new EndGame(), SettingsReader.getSettings().getGameDuration() * 1000);
        thread.updateClock(SettingsReader.getSettings().getGameDuration() * 1000);

        class UpdateTime extends TimerTask {
            public void run() {
                thread.decreaseTime();
            }
        }

        Timer t1 = new Timer();
        timers.add(t1);
        t1.scheduleAtFixedRate(new UpdateTime(), 0L, 1000L);
        GAMESTARTED = true;
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
        server = new Server((Stack<Player>) settings.getPlayers().clone(), this);
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
        t[c.getX()][c.getY()] = Types.PERSON;
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
            c = me;

            if (!canMove(e, c)) return false;

            moveClean((Player) c);

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

            if (!SERVER_MODE) {
                // update the server with the new position
                String msg = "move " + c.getID() + " " + c.getX() + " " + c.getY();
                new ClientConnectorTask().execute(msg);
            }
            movePlace((Player) c);
            return true;
        }
    }

    public boolean dropBomb(Integer id) {
        synchronized (lock) {
            Controllable c = null;

            if (id < 5) {
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

                Timer t = new Timer();
                timers.add(t);
                t.schedule(new BlowBombTimerTask(c.getX(), c.getY(), c, t), SettingsReader.getSettings().getExplosionTimeout() * 1000);

                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * register the player on the server
     *
     * @param name
     * @param main
     */
    public void register(String name, Main main) {
        this.main = main;
        if (SERVER_MODE) {
            // dirty hack to prevent using WifiP2pInfo
            if (info == null) {
                server.addPlayer(name, NetworkUtils.getIPAddress());
            } else {
                server.addPlayer(name, info.groupOwnerAddress.getHostAddress());
            }
        } else {
            String ipAddress = NetworkUtils.getIPAddress();
            String msg = "register " + name + " " + ipAddress;
            new ClientConnectorTask().execute(msg);
        }
    }

    public void ackReg(Integer id, int xpos, int ypos) {
        // TODO: there should be a method to add the Type of the player and set the name
        Player player = new Player(id, main.getPlayerName(), xpos, ypos, this, main);
        t[xpos][ypos] = Types.PERSON;
        p.put(id, player);
        this.me = player;
        main.createPlayer(player);
    }

    /**
     * addPlayer
     * add a new player to the game. Functions as both creating the player of this
     * game and adding a new opponent
     *
     * @param id
     * @param name
     * @param xpos
     * @param ypos
     */
    public void addPlayer(Integer id, String name, int xpos, int ypos) {
        // TODO: there should be a method to add the Type of the player and set the name
        Player player = new Player(id, name, xpos, ypos, this, main);
        t[xpos][ypos] = Types.PERSON;
        p.put(id, player);
        Main.game.signalRedraw();
    }

    public void moveAnotherSmelly(Integer id, String direction) {
        Player smelly = p.get(id);

        moveClean(smelly);

        if (direction.equals("down")) {
            smelly.incrX();
        } else if (direction.equals("up")) {
            smelly.decrX();
        } else if (direction.equals("left")) {
            smelly.decrY();
        } else {
            smelly.incrY();
        }
        movePlace(smelly);
        Main.game.signalRedraw();
    }

    public void moveRobot(Integer id, Integer xpos, Integer ypos) {
        Robot robot = r.get(id);
        int oldx = robot.getX();
        int oldy = robot.getY();

        if (oldx < xpos)
            robot.incrX();
        if (oldx > xpos)
            robot.decrX();
        if (oldy < ypos)
            robot.incrY();
        if (oldy > ypos)
            robot.decrY();

        t[oldx][oldy] = Types.NULL;
        t[xpos][ypos] = Types.ROBOT;
        Main.game.signalRedraw();
    }

    public void dumpBanana(Integer xpos, Integer ypos) {
        t[xpos][ypos] = Types.PERSONANDBOMB;
    }

    class BlowBombTimerTask extends TimerTask {
        private int x;
        private int y;
        private Controllable controllable;
        private Timer timer;

        public BlowBombTimerTask(int x, int y, Controllable c, Timer timer) {
            this.x = x;
            this.y = y;
            this.controllable = c;
            this.timer = timer;
        }

        public void run() {
            synchronized (lock) {
                t[x][y] = Types.EXPLOSION;
                controllable.toggleBomb();
                if (cleanTab(x, y)) {
                    thread.smellyDied();
                    cancelTimers();
                    GAMEOVER = true;
                }
            }

            timer.cancel();
            timer.purge();

            if (!GAMEOVER) {
                Timer t = new Timer();
                timers.add(t);
                t.schedule(new CleanBombTimerTask(x, y, t), SettingsReader.getSettings().getExplosionDuration() * 1000);
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

        private boolean killControllables(int x, int y) {
            ArrayList<Controllable> controllables = new ArrayList<Controllable>();
            controllables.addAll(r.values());
            controllables.addAll(p.values());

            boolean returnValue = false;

            for (Controllable c : controllables) {
                if (c.getX() == x && c.getY() == y) {

                    if (c instanceof Player) {
                        t[c.getX()][c.getY()] = Types.EXPLOSION;
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

        private void setExplosion(int x, int y) {
            if (t[x][y].equals(Types.BOMB)) {
                t[x][y] = Types.EXPLOSIONANDBOMB;
            } else {
                t[x][y] = Types.EXPLOSION;
            }
        }
    }

    private class CleanBombTimerTask extends TimerTask {
        private int x;
        private int y;
        private Timer timer;

        public CleanBombTimerTask(int x, int y, Timer t) {
            this.x = x;
            this.y = y;
            this.timer = t;
        }

        public void run() {
            synchronized (lock) {
                t[x][y] = Types.NULL;
                cleanExplosion(x, y);
            }
            timer.cancel();
            timer.purge();
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

        private void cleanExplosionAux(int x, int y) {
            if (t[x][y].equals(Types.EXPLOSIONANDBOMB)) {
                t[x][y] = Types.BOMB;
            } else {
                t[x][y] = Types.NULL;
            }
        }
    }
}
