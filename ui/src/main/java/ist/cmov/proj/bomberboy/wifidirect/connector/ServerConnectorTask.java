package ist.cmov.proj.bomberboy.wifidirect.connector;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.ui.Main;
import ist.cmov.proj.bomberboy.wifidirect.service.ClientService;

/**
 * Created by duarte on 09-05-2014.
 */
public class ServerConnectorTask extends AsyncTask<String, Void, Integer> {

    private final int SOCKET_TIMEOUT = 5000;

    protected Integer doInBackground(String... strings) {
        // validate input parameters
        if (strings.length <= 0) {
            return 0;
        }
        Socket client = new Socket();
        // connect to the client and send the message
        try {
            Log.d(Main.TAG, "Opening server socket : ");
            client.bind(null);
            client.connect((new InetSocketAddress(strings[1], ClientService.PEER_PORT)), SOCKET_TIMEOUT);
            Log.d(Main.TAG, "Server socket : " + client.isConnected());
            PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
            printwriter.write(strings[0]);
            printwriter.flush();
            printwriter.close();
        } catch (UnknownHostException e) {
            Log.e("SERVER", e.getMessage(), e);
            return 0;
        } catch (IOException e) {
            Log.e("SERVER", e.getMessage(), e);
            return 0;
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.e("SERVER", e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
        return 1;
    }

    protected void onPostExecute(Long result) {
        return;
    }
}
