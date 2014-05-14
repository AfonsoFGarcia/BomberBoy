package ist.cmov.proj.bomberboy.wifidirect;

import android.content.BroadcastReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import ist.cmov.proj.bomberboy.control.Controllable;
import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.control.robots.Robot;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;
import ist.cmov.proj.bomberboy.status.Types;
import ist.cmov.proj.bomberboy.utils.NetworkUtils;
import ist.cmov.proj.bomberboy.utils.SettingsReader;
import ist.cmov.proj.bomberboy.wifidirect.connector.BroadcastMessage;
import ist.cmov.proj.bomberboy.wifidirect.connector.ServerConnectorTask;

/**
 * Created by duarte on 09-05-2014.
 */
public class Server {

    private static final String TAG = "SERVER";

    private Stack<Player> playerStack;
    private HashMap<Integer, Player> players = new HashMap<Integer, Player>();
    private HashMap<Integer, String> playersURL = new HashMap<Integer, String>();
    private HashMap<Integer, Robot> robots = new HashMap<Integer, Robot>();
    private GameStatus status;
    private Object lock = new Object();
    private Types[][] map;
    protected ArrayList<Timer> timers = new ArrayList<Timer>();
    private Collection<String> peers = new ArrayList<String>();
    private boolean GAMEOVER = false;

    public Server(Stack<Player> stack, HashMap<Integer, Robot> r, GameStatus status) {
        this.playerStack = stack;
        this.robots = r;
        this.status = status;
        try {
            for (Robot rob : r.values()) {
                rob.start();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        map = status.getMap();
    }

    public void smellMove(Integer id, Integer xpos, Integer ypos) {
        Player p = players.get(id);

        String name = p.getName();
        // placeholder debug message
        System.err.println("Smelly " + name + " moved to " + xpos + ", " + ypos);

        if (id != status.getMe().getID())
            status.moveAnotherSmelly(id, xpos, ypos);

        // comunicate changes to other players
        Collection<Player> playerColl = players.values();
        for (Player o : playerColl) {
            if (o.getID() == p.getID() || o.getID() == status.getMe().getID())
                continue;

            String msg = "move " + id + " " + xpos + " " + ypos;
            ServerConnectorTask broadcast = new ServerConnectorTask();
            broadcast.execute(msg, o.getUrl());
        }
    }

    public void bananaDump(Integer id, Integer xpos, Integer ypos) {
        Player p = players.get(id);
        String url = p.getUrl();
        HashMap<Integer, String> urlsClone = (HashMap<Integer, String>) playersURL.clone();
        Collection<String> urls = urlsClone.values();
        urls.remove(url);
        BroadcastMessage bm = new BroadcastMessage("banana " + id + " " + xpos + " " + ypos, urls);
        bm.start();
        status.dumpBanana(id, xpos, ypos);

        // calling the timer task to blow the bombs and check for any deaths by bad smell
        Timer t = new Timer();
        timers.add(t);
        t.schedule(new BlowBombTimerTaskServer(xpos, ypos, players.get(id), t), SettingsReader.getSettings().getExplosionTimeout() * 1000);
    }

    public boolean addPlayer(String name, String url) {
        if (!playerStack.empty()) {
            Player p = playerStack.pop();
            int id = p.getID();
            p.setUrl(url);
            p.setName(name);
            playersURL.put(id, url);
            peers.add(url);
            // placeholder message for the server
            Log.i(TAG, "Player " + name + " joined a new game, with ID: " + id + "\nand url " + url);

            // inform the player (ack register) we added him with pair (id, pos)
            String msg = "ackReg " + id + " " + p.getX() + " " + p.getY();
            ServerConnectorTask inform = new ServerConnectorTask();
            inform.execute(msg, url);

            if (players.size() > 0) {
                // TODO: Try to use a BroadcastConnectorTask
                Collection<Player> playerColl = players.values();
                // inform the player about the other players in game
                for (Player c : playerColl) {
                    String player = "newplayer " + c.getID() + " " + c.getX() + " " + c.getY() + " " + c.getName();
                    ServerConnectorTask update = new ServerConnectorTask();
                    update.execute(player, url);

                    // if there are other players in game, let's inform them
                    if (c.getID() == status.getMe().getID()) {
                        status.addPlayer(id, name, p.getX(), p.getY());
                        continue;
                    }
                    String other = "newplayer " + id + " " + p.getX() + " " + p.getY() + " " + name;
                    ServerConnectorTask others = new ServerConnectorTask();
                    others.execute(other, c.getUrl());
                }
            }

            players.put(id, p);
            status.updatePlayers(players); // re-referencing, however it's not extremely needed
            return true;
        }

        return false;
    }

    public boolean addMe(String name, String url) {
        if (!playerStack.empty()) {
            Player p = playerStack.pop();
            int id = p.getID();
            p.setUrl(url);
            p.setName(name);
            playersURL.put(id, url);

            // placeholder message for the server
            Log.i(TAG, "Player " + name + " joined a new game, with ID: " + id + "\nand url " + url);

            status.ackReg(id, p.getX(), p.getY());
            players.put(id, p);
            status.updatePlayers(players); // adds a reference to the status for the server players
            return true;
        }

        return false;
    }

    private void stopRobot(int id) {
        Robot r = robots.get(id);
        r.stopRobot();
        map[r.getX()][r.getY()] = Types.NULL;
        BroadcastMessage msg = new BroadcastMessage("poofRobot " + r.getX() + " " + r.getY(), peers);
        msg.start();
    }

    private void killSmelly(int meltedID, int smellyID) {
        String meltedURL = playersURL.get(meltedID);
        String smellyURL = playersURL.get(smellyID);

        if(meltedID == smellyID) {
            BroadcastMessage selfKill = new BroadcastMessage("suicide", meltedURL);
            selfKill.start();
        } else {
            Player death = players.get(smellyID);
            Player poorGuy = players.get(meltedID);
            death.increaseScore(SettingsReader.getSettings().getPointsPerPlayer());
            poorGuy.interrupt();
            Collection<String> urls = new ArrayList<String>();
            urls.add(smellyURL);
            urls.add(meltedURL);
            BroadcastMessage deathFlow = new BroadcastMessage("killplayer " + smellyID + " " + meltedID, urls);
            deathFlow.start();
        }
    }

    private void increaseScore(int smellyID) {
        if(smellyID == status.getMe().getID()) {
            players.get(smellyID).increaseScore(SettingsReader.getSettings().getPointsPerRobot());
        } else {
            BroadcastMessage msg = new BroadcastMessage("increaseScore " + SettingsReader.getSettings().getPointsPerRobot(),
                    playersURL.get(smellyID));
            msg.start();
        }
    }
    /**
     * Robots movement
     */
    /**
     * move
     *
     * @param e
     * @param id computes and updates the map for a move call from a robot thread
     */
    public boolean move(Movements e, Integer id) {
        synchronized (lock) {
            Robot r;
            r = robots.get(id);

            if (!status.canMove(e, r)) return false;

            status.moveClean(r);
            if (e.equals(Movements.DOWN)) {
                r.incrX();
            } else if (e.equals(Movements.UP)) {
                r.decrX();
            } else if (e.equals(Movements.LEFT)) {
                r.decrY();
            } else {
                r.incrY();
            }
            status.movePlace(r);
            String msg = "robot " + r.getID() + " " + r.getX() + " " + r.getY();
            BroadcastMessage bm = new BroadcastMessage(msg, peers);
            bm.start();

            return true;
        }
    }

    /**
     * ***********
     * Timer tasks
     * *************
     */
    class BlowBombTimerTaskServer extends TimerTask {
        private int x;
        private int y;
        private Controllable controllable;
        private Timer timer;

        public BlowBombTimerTaskServer(int x, int y, Controllable c, Timer timer) {
            this.x = x;
            this.y = y;
            this.controllable = c;
            this.timer = timer;
        }

        public void run() {
            synchronized (lock) {
                status.drawExplosion(x, y);
                controllable.toggleBomb();
                if (cleanTab(x, y)) {
                    //thread.smellyDied();
                    //cancelTimers();
                    //GAMEOVER = true;
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

            for (int i = 1; i < status.RANGE; i++) {
                if (y + i < status.SIZE && !map[x][y + i].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x, y + i);
                    status.drawExplosion(x, y + i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < status.RANGE; i++) {
                if (x + i < status.SIZE && !map[x + i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x + i, y);
                    status.drawExplosion(x + i, y);
                } else {
                    break;
                }
            }
            for (int i = 1; i < status.RANGE; i++) {
                if (y - i >= 0 && !map[x][y - i].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x, y - i);
                    status.drawExplosion(x, y - i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < status.RANGE; i++) {
                if (x - i >= 0 && !map[x - i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killControllables(x - i, y);
                    status.drawExplosion(x - i, y);
                } else {
                    break;
                }
            }
            return returnValue;
        }

        private boolean killControllables(int x, int y) {
            ArrayList<Controllable> controllables = new ArrayList<Controllable>();
            controllables.addAll(robots.values());
            controllables.addAll(players.values());

            boolean returnValue = false;

            for (Controllable c : controllables) {
                if (c.getX() == x && c.getY() == y) {

                    if (c instanceof Player) {
                        map[c.getX()][c.getY()] = Types.EXPLOSION;
                        killSmelly(c.getID(), controllable.getID());
                        returnValue = true;
                    } else {
                        controllable.increaseScore(SettingsReader.getSettings().getPointsPerRobot());
                        increaseScore(controllable.getID());
                        stopRobot(c.getID());
                    }
                }
            }

            return returnValue;
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
                map[x][y] = Types.NULL;
                cleanExplosion(x, y);
            }
            timer.cancel();
            timer.purge();
        }

        private void cleanExplosion(int x, int y) {
            for (int i = 0; i < status.RANGE; i++) {
                if (y + i < status.SIZE && !map[x][y + i].equals(Types.WALL)) {
                    cleanExplosionAux(x, y + i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < status.RANGE; i++) {
                if (x + i < status.SIZE && !map[x + i][y].equals(Types.WALL)) {
                    cleanExplosionAux(x + i, y);
                } else {
                    break;
                }
            }
            for (int i = 1; i < status.RANGE; i++) {
                if (y - i >= 0 && !map[x][y - i].equals(Types.WALL)) {
                    cleanExplosionAux(x, y - i);
                } else {
                    break;
                }
            }
            for (int i = 1; i < status.RANGE; i++) {
                if (x - i >= 0 && !map[x - i][y].equals(Types.WALL)) {
                    cleanExplosionAux(x - i, y);
                } else {
                    break;
                }
            }
        }

        private void cleanExplosionAux(int x, int y) {
            if (map[x][y].equals(Types.EXPLOSIONANDBOMB)) {
                map[x][y] = Types.BOMB;
            } else {
                map[x][y] = Types.NULL;
            }
        }
    }
}

