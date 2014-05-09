package ist.cmov.proj.bomberboy.wifidirect.connector;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.wifidirect.service.ServerService;

/**
 * Created by duarte on 24-04-2014.
 */
public class ClientConnectorTask extends AsyncTask<String, Void, Integer> {

    protected Integer doInBackground(String... strings) {
        // validate input parameters
        if (strings.length <= 0) {
            return 0;
        }
        // connect to the server and send the message
        try {
            // hard-typed server url
            Socket server = new Socket(GameStatus.info.groupOwnerAddress.getHostAddress(),
                    ServerService.GROUP_OWNER_PORT);
            PrintWriter printwriter = new PrintWriter(server.getOutputStream(), true);
            printwriter.write(strings[0]);
            printwriter.flush();
            printwriter.close();
            server.close();
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
