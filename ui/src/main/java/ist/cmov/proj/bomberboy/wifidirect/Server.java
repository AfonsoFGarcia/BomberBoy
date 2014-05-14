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
    protected ArrayList<Timer> timers = new ArrayList<Timer>();
    private Collection<String> peers = new ArrayList<String>();

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
    }

    public void smellMove(Integer id, Integer xpos, Integer ypos) {
        Player p = players.get(id);

        String name = p.getName();
        // placeholder debug message
        System.err.println("Smelly " + name + " moved to " + xpos + ", " + ypos);

        if(id != status.getMe().getID())
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
        Collection<String> peers = urlsClone.values();
        peers.remove(url);
        BroadcastMessage bm = new BroadcastMessage("banana " + id + " " + xpos + " " + ypos, peers);
        bm.start();
        status.dumpBanana(id, xpos, ypos);


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
                    if(c.getID() == status.getMe().getID())
                        continue;
                    String other = "newplayer " + id + " " + p.getX() + " " + p.getY() + " " + name;
                    ServerConnectorTask others = new ServerConnectorTask();
                    others.execute(other, c.getUrl());
                }
            }

            players.put(id, p);
            status.updatePlayers(players);
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
            status.updatePlayers(players);
            return true;
        }

        return false;
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

            // Maybe for later!!
            // if (diedInNuclearFallout(c) && c instanceof Player) {
            //     thread.smellyDied();
            //     return true;
            // }

            status.movePlace(r);
            String msg = "robot " + r.getID() + " " + r.getX() + " " + r.getY();
            Collection<String> peers = playersURL.values();
            peers.remove(status.getMe().getUrl());
            BroadcastMessage bm = new BroadcastMessage(msg, peers);
            bm.start();

            return true;
        }
    }

    /**************
     * Timer tasks
     **************
     */

}

