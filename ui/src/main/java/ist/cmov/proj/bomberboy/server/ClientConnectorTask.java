package ist.cmov.proj.bomberboy.server;

import android.os.AsyncTask;
import android.util.Log;

import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

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
            Socket client = new Socket("192.168.1.5", 8086);
            PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
            printwriter.write(strings[0]);
            printwriter.flush();
            printwriter.close();
            client.close();
        } catch (UnknownHostException e) {
            Log.d("SERVER", e.getMessage());
            return 0;
        } catch (IOException e) {
            Log.d("SERVER", e.getMessage());
            return 0;
        }
        return 1;
    }

    protected void onPostExecute(Long result) {
        return;
    }
}
