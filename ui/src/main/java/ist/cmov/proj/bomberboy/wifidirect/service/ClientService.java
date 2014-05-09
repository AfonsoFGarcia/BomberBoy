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

/**
 * Created by duarte on 09-05-2014.
 */
public class ClientService extends Service {

    public static final String TAG = "SERVERSERVICE";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_address";
    public static final int PEER_PORT = 8101;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int DEFAULT = START_STICKY;
        int FAIL = START_NOT_STICKY;

        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "Server not started because there was no information to launch it.");
            return FAIL;
        }

        if (!GameStatus.SERVER_MODE) {
            // if this device is the server node
            Client peer = new Client();
            new Thread(peer).start();
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
        ClientService getService() {
            return ClientService.this;
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
                Main.g.ackReg(id, xpos, ypos);
            }
        } else {
            if (command.equals("move")) {
                Integer id = Integer.parseInt(tokens[1]);
                String direction = tokens[2];
                if (direction.equals("still"))
                    return;
                Main.g.moveAnotherSmelly(id, direction);
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
                Integer xpos = Integer.parseInt(tokens[1]);
                Integer ypos = Integer.parseInt(tokens[2]);
                Main.g.dumpBanana(xpos, ypos);
                return;
            }
        }

    }

    class Client implements Runnable {

        private ServerSocket clientSocket;

        Client() {
            try {
                clientSocket = new ServerSocket(8101);
            } catch (IOException e) {
                Log.e(TAG, "Cannot open client socket : ", e);
            }
        }

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
    }


}
