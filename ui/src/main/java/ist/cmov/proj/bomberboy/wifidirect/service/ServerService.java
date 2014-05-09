package ist.cmov.proj.bomberboy.wifidirect.service;

import android.app.Service;
import android.content.Intent;
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
import ist.cmov.proj.bomberboy.ui.Main;
import ist.cmov.proj.bomberboy.wifidirect.Server;

/**
 * Created by duarte on 08-05-2014.
 */
public class ServerService extends Service {

    public static final String TAG = "SERVERSERVICE";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_address";
    public static final int GROUP_OWNER_PORT = 8100;
    private ist.cmov.proj.bomberboy.wifidirect.Server server;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int DEFAULT = START_STICKY;
        int FAIL = START_NOT_STICKY;

        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "Server not started because there was no information to launch it.");
            return FAIL;
        }

        if (GameStatus.SERVER_MODE) {
            // if this device is the server node
            server = Main.g.getServerObject();
            ServerThread host = new ServerThread();
            new Thread(host).start();
            Log.i(TAG, "Started server service with id " + startID + " : " + intent);
            return DEFAULT;
        } else {
            return FAIL;
        }
    }

    @Override
    public void onDestroy() {
    }


    public class ServerBinder extends Binder {
        ServerService getService() {
            return ServerService.this;
        }
    }

    private final IBinder mBinder = new ServerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void parseMsg(String m) {
        String[] tokens = m.split("\\s+");
        String command = tokens[0];

        if (command.equals("register"))
            joinGame(tokens);

        if (command.equals("move"))
            updateTrashman(tokens);

        if (command.equals("banana"))
            updateBanana(tokens);
    }

    private void joinGame(String[] params) {
        String playerName = params[1];
        String url = params[2];

        boolean success = server.addPlayer(playerName, url);
        if (!success) {
            System.err.println("Player " + playerName + " attempted to join a full game.");
            // do something... call the trashman to make this player run away with his smell
            return;
        }
    }

    private void updateTrashman(String[] params) {
        Integer id = Integer.parseInt(params[1]);
        Integer xnew = Integer.parseInt(params[2]);
        Integer ynew = Integer.parseInt(params[3]);

        try {
            server.smellMove(id, xnew, ynew);
        } catch (NullPointerException npe) {
            // probably game has not been initialized
            System.err.println("NullPointerException: " + npe.getMessage());
        }

    }

    private void updateBanana(String[] params) {
        Integer id = Integer.parseInt(params[1]);
        Integer bananaX = Integer.parseInt(params[2]);
        Integer bananaY = Integer.parseInt(params[3]);

        try {
            server.bananaDump(id, bananaX, bananaY);
        } catch (NullPointerException npe) {
            System.err.println("NullPointerException: " + npe.getMessage());
        }
    }

    class ServerThread implements Runnable {

        private ServerSocket serverSocket;

        ServerThread() {
            try {
                serverSocket = new ServerSocket(8100);
            } catch (IOException e) {
                Log.e(TAG, "Cannot open server socket : ", e);
            }
        }

        @Override
        public void run() {
            while (GameStatus.SERVER_MODE) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    InputStreamReader streamReader =
                            new InputStreamReader(clientSocket.getInputStream());
                    BufferedReader bufferedReader =
                            new BufferedReader(streamReader);
                    String msg = bufferedReader.readLine();
                    while (!msg.isEmpty()) {
                        System.out.println(msg); // placeholder for debug
                        parseMsg(msg);
                        msg = "";
                    }
                    streamReader.close();
                    clientSocket.close();

                } catch (IOException e) {
                    Log.e(TAG, "Problem in message reading : ", e);
                }
            }
        }
    }


}
