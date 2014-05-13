package ist.cmov.proj.bomberboy.wifidirect;

import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.control.robots.Robot;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.utils.NetworkUtils;
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

    public Server(Stack<Player> stack, GameStatus status) {
        this.playerStack = stack;
        this.status = status;
    }

    public void smellMove(Integer id, String dir) {
        Player p = players.get(id);

        String name = p.getName();
        // placeholder debug message
        System.err.println("Smelly " + name + " moved to " + p.getX() + ", " + p.getY() + "\ndirection " + dir);

        if(id != status.getMe().getID())
            status.moveAnotherSmelly(id, dir);

        // comunicate changes to other players
        Collection<Player> playerColl = players.values();
        for (Player o : playerColl) {
            if (o.getID() == p.getID() || o.getID() == status.getMe().getID())
                continue;

            String msg = "move " + id + " " + dir;
            ServerConnectorTask broadcast = new ServerConnectorTask();
            broadcast.execute(msg, o.getUrl());
        }
    }

    public void bananaDump(Integer id, Integer xpos, Integer ypos) {

    }

    public boolean addPlayer(String name, String url) {
        if (!playerStack.empty()) {
            Player p = playerStack.pop();
            int id = p.getID();
            p.setUrl(url);
            p.setName(name);
            playersURL.put(id, url);

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

            /* IGNORE FOR NOW
            if (players.size() > 1) {
                // TODO: Try to use a BroadcastConnectorTask
                Collection<Player> playerColl = players.values();
                // inform the player about the other players in game
                for (Player c : playerColl) {
                    String player = "newplayer " + c.getID() + " " + c.getX() + " " + c.getY() + " " + c.getName();
                    ServerConnectorTask update = new ServerConnectorTask();
                    update.execute(player, url);
                }
                // if there are other players in game, let's inform them
                for (Player o : playerColl) {
                    String other = "newplayer " + id + " " + p.getX() + " " + p.getY() + " " + name;
                    ServerConnectorTask others = new ServerConnectorTask();
                    others.execute(other, o.getUrl());
                }
            } */
            players.put(id, p);
            status.updatePlayers(players);
            return true;
        }

        return false;
    }

    /**
     * Outgoing requests
     */

}
