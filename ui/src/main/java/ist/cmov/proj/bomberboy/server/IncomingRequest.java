package ist.cmov.proj.bomberboy.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.ui.Main;

/**
 * Created by duarte on 28-04-2014.
 */
public class IncomingRequest extends Thread {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;

    public IncomingRequest(int port) {

        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException ioe) {
            // eat exception for now
        }
    }
    @Override
    public void run() {
        while(true) {
            try {
                clientSocket = serverSocket.accept();
                inputStreamReader =
                        new InputStreamReader(clientSocket.getInputStream());
                bufferedReader =
                        new BufferedReader(inputStreamReader);
                String msg = bufferedReader.readLine();
                while (!msg.isEmpty()) {
                    System.out.println(msg);
                    parseMsg(msg);
                    msg = "";
                }
                inputStreamReader.close();
                clientSocket.close();

            } catch (IOException ex) {
                System.out.println("Problem in message reading");
                break;
            }

        }
    }

    private void parseMsg(String msg) {

        String[] tokens = msg.split("\\s+");
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
}
