package ist.cmov.proj.bomberboy.wifidirect.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.ui.Main;
import ist.cmov.proj.bomberboy.ui.R;

/**
 * Created by duarte on 08-05-2014.
 */
public class ServerService extends Service {

    private final static int id = 8020;
    public static final String TAG = "SERVERSERVICE";
    public static final int GROUP_OWNER_PORT = 8988;
    private ist.cmov.proj.bomberboy.wifidirect.Server server;
    private ServerSocket serverSocket;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            serverSocket = new ServerSocket(GROUP_OWNER_PORT);
        } catch (IOException e) {
            Log.e(TAG, "Cannot open server socket : ", e);
        }
        new Thread(serverThread).start();
        Notification notification = new Notification.Builder(this)
                .setContentTitle("BomberBoy Server")
                .setContentText("Running")
                .setSmallIcon(R.drawable.robot).build();
        startForeground(id, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int DEFAULT = START_STICKY;
        int FAIL = START_NOT_STICKY;

        if (GameStatus.SERVER_MODE) {
            // if this device is the server node
            server = Main.g.getServerObject();
            Log.i(TAG, "Server service with id " + startID + " : " + intent);
            return DEFAULT;
        } else {
            return FAIL;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the server socket due to : " + e.getMessage(), e);
        }
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
        }
    }

    private void updateTrashman(String[] params) {
        Integer id = Integer.parseInt(params[1]);
        Integer xpos = Integer.parseInt(params[2]);
        Integer ypos = Integer.parseInt(params[3]);

        try {
            server.smellMove(id, xpos, ypos);
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

    private Runnable serverThread = new Runnable() {
        @Override
        public void run() {
            while (GameStatus.SERVER_MODE) {
                Socket socket;
                InputStreamReader streamReader;
                try {
                    socket = serverSocket.accept();
                    InetAddress source = socket.getInetAddress();
                    streamReader =
                            new InputStreamReader(socket.getInputStream());
                    BufferedReader bufferedReader =
                            new BufferedReader(streamReader);
                    String msg = bufferedReader.readLine();
                    StringBuilder appended = new StringBuilder(msg);
                    appended.append(" " + source.getHostAddress());
                    msg = appended.toString();
                    while (!msg.isEmpty()) {
                        System.out.println(msg); // placeholder for debug
                        parseMsg(msg);
                        msg = "";
                    }
                    streamReader.close();
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Problem in message reading : ", e);
                }
            }
        }
    };
}
