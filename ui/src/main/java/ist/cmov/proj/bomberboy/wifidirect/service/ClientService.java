package ist.cmov.proj.bomberboy.wifidirect.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.ui.BomberView;
import ist.cmov.proj.bomberboy.ui.Main;

/**
 * Created by duarte on 09-05-2014.
 */
public class ClientService extends Service {

    public static final String TAG = "SERVERSERVICE";
    public static final int PEER_PORT = 8989;
    private ServerSocket clientSocket;
    private Thread thread;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            clientSocket = new ServerSocket(PEER_PORT);
        } catch (IOException e) {
            Log.e(TAG, "Cannot open client socket : ", e);
        }
        thread = new Thread(clientThread);
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int DEFAULT = START_STICKY;
        int FAIL = START_NOT_STICKY;

        if (!GameStatus.SERVER_MODE) {
            // if this device is NOT the server node
            Log.i(TAG, "Client service with id " + startID + " : " + intent);
            return DEFAULT;
        } else {
            return FAIL;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            thread.interrupt();
            Thread.sleep(1000);
            clientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket due to : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public class ClientBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }

    private final IBinder mBinder = new ClientBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent i) {
        try {
            thread.interrupt();
            Thread.sleep(1000);
            clientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket due to : " + e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void parseMsg(String m) {
        String[] tokens = m.split("\\s+");
        String command = tokens[0];
        if (Main.g == null)
            return;

        if (command.equals("newplayer")) {
            Integer id = Integer.parseInt(tokens[1]);
            Integer xpos = Integer.parseInt(tokens[2]);
            Integer ypos = Integer.parseInt(tokens[3]);
            String name = tokens[4];
            Main.g.addPlayer(id, name, xpos, ypos);
            return;
        }

        if (!GameStatus.GAMESTARTED) {
            if (command.equals("ackReg")) {
                Integer id = Integer.parseInt(tokens[1]);
                Integer xpos = Integer.parseInt(tokens[2]);
                Integer ypos = Integer.parseInt(tokens[3]);
                Integer timeleft = Integer.parseInt(tokens[4]);
                System.out.println(id + " " + xpos + " " + ypos);
                BomberView.timeLeft = timeleft;
                Main.g.ackReg(id, xpos, ypos);
            }
        } else {
            if (command.equals("move")) {
                Integer id = Integer.parseInt(tokens[1]);
                Integer xpos = Integer.parseInt(tokens[2]);
                Integer ypos = Integer.parseInt(tokens[3]);
                Main.g.moveAnotherSmelly(id, xpos, ypos);
                return;
            }
            if (command.equals("robot")) {
                Integer id = Integer.parseInt(tokens[1]);
                Integer xpos = Integer.parseInt(tokens[2]);
                Integer ypos = Integer.parseInt(tokens[3]);
                Main.g.moveRobot(id, xpos, ypos);
                return;
            }
            if (command.equals("banana")) {
                Integer id = Integer.parseInt(tokens[1]);
                Integer xpos = Integer.parseInt(tokens[2]);
                Integer ypos = Integer.parseInt(tokens[3]);
                Main.g.dumpBanana(id, xpos, ypos);
                return;
            }
            if (command.equals("killplayer")) {
                Integer killer = Integer.parseInt(tokens[1]);
                Integer killed = Integer.parseInt(tokens[2]);
                Main.g.killSmelly(killer, killed);
                return;
            }
            if (command.equals("suicide")) {
                Boolean b = Boolean.parseBoolean(tokens[1]);
                Main.g.suicide(b);
                return;
            }
            if (command.equals("increaseScore")) {
                Integer points = Integer.parseInt(tokens[1]);
                Main.g.getMe().increaseScore(points);
                return;
            }
            if (command.equals("poofRobot")) {
                Integer x = Integer.parseInt(tokens[1]);
                Integer y = Integer.parseInt(tokens[2]);
                Main.g.cleanRobot(x, y);
                return;
            }
            if (command.equals("playerdied")) {
                Integer x = Integer.parseInt(tokens[1]);
                Integer y = Integer.parseInt(tokens[2]);
                Main.g.cleanPlayer(x, y);
            }

        }

    }

    private Runnable clientThread = new Runnable() {
        @Override
        public void run() {
            while (!GameStatus.SERVER_MODE) {
                try {
                    Socket serverSocket = clientSocket.accept();
                    InputStreamReader streamReader =
                            new InputStreamReader(serverSocket.getInputStream());
                    BufferedReader bufferedReader =
                            new BufferedReader(streamReader);
                    String msg = bufferedReader.readLine();
                    while (!msg.isEmpty()) {
                        System.out.println(msg); // placeholder for debug
                        parseMsg(msg);
                        msg = "";
                    }
                    streamReader.close();
                    serverSocket.close();

                } catch (IOException e) {
                    Log.e(TAG, "Problem in message reading : ", e);
                }
            }
        }
    };
}
