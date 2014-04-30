package ist.cmov.proj.bomberboy.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

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

                String msg;
                msg = bufferedReader.readLine();
                while(!msg.isEmpty())
                {
                    System.out.println(msg);
                    parseMsg(msg);
                    msg = "";
                }


                inputStreamReader.close();
                clientSocket.close();

            }
            catch (IOException ex)
            {
                System.out.println("Problem in message reading");
                break;
            }

        }
    }

    private void parseMsg(String msg) {
        String[] tokens = msg.split(" ");
        String command = tokens[0];
        if(command.equals("newplayer")) {
            String id = tokens[1];
            String name = tokens[2];
            Main.g.addPlayer(id, name);
        }

        if(command.equals("move")) {
            String playerID = tokens[1];
            String direction = tokens[2];
            Integer id = Integer.parseInt(playerID);
            Main.g.moveAnotherSmelly(id, direction);
        }
    }
}
