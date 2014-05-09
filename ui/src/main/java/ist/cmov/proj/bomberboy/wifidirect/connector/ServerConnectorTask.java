package ist.cmov.proj.bomberboy.wifidirect.connector;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.wifidirect.service.ClientService;

/**
 * Created by duarte on 09-05-2014.
 */
public class ServerConnectorTask extends AsyncTask<String, Void, Integer> {

    protected Integer doInBackground(String... strings) {
        // validate input parameters
        if (strings.length <= 0) {
            return 0;
        }
        // connect to the client and send the message
        try {
            Socket client = new Socket(strings[1],
                    ClientService.PEER_PORT);
            PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
            printwriter.write(strings[0]);
            printwriter.flush();
            printwriter.close();
            client.close();
        } catch (UnknownHostException e) {
            Log.e("SERVER", e.getMessage(), e);
            return 0;
        } catch (IOException e) {
            Log.e("SERVER", e.getMessage(), e);
            return 0;
        }
        return 1;
    }

    protected void onPostExecute(Long result) {
        return;
    }
}
