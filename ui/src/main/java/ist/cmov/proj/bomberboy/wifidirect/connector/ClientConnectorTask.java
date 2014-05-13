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
import ist.cmov.proj.bomberboy.wifidirect.service.ServerService;

/**
 * Created by duarte on 24-04-2014.
 */
public class ClientConnectorTask extends AsyncTask<String, Void, Integer> {

    private final int SOCKET_TIMEOUT = 10000;

    protected Integer doInBackground(String... strings) {
        // validate input parameters
        if (strings.length <= 0) {
            return 0;
        }
        Socket server = new Socket();
        // connect to the server and send the message
        try {
            Log.d(Main.TAG, "Opening client socket : ");
            server.bind(null);
            server.connect((new InetSocketAddress(strings[1], ServerService.GROUP_OWNER_PORT)), SOCKET_TIMEOUT);
            Log.d(Main.TAG, "Client socket : " + server.isConnected());
            PrintWriter printwriter = new PrintWriter(server.getOutputStream(), true);
            printwriter.write(strings[0]);
            printwriter.flush();
            printwriter.close();
        } catch (UnknownHostException e) {
            Log.e("CLIENT", e.getMessage(), e);
            return 0;
        } catch (IOException e) {
            Log.e("CLIENT", e.getMessage(), e);
            return 0;
        } finally {
            if (server != null && server.isConnected()) {
                try {
                    server.close();
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
