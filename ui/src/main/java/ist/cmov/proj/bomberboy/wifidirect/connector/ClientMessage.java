package ist.cmov.proj.bomberboy.wifidirect.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import ist.cmov.proj.bomberboy.wifidirect.Server;
import ist.cmov.proj.bomberboy.wifidirect.service.ClientService;
import ist.cmov.proj.bomberboy.wifidirect.service.ServerService;

/**
 * Created by duarte on 14-05-2014.
 */
public class ClientMessage extends Thread {

    private Collection<String> urls;
    private String msg;

    public ClientMessage(String m, Collection<String> playersURL) {
        msg = m;
        urls = playersURL;
    }

    public ClientMessage(String m, String url) {
        msg = m;
        urls = new ArrayList<String>();
        urls.add(url);
    }

    @Override
    public void run() {
        try {
            for (String s : urls) {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress(s, ServerService.GROUP_OWNER_PORT));

                PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                pw.write(msg);
                pw.flush();
                pw.close();
                sock.close();
            }
        } catch (UnknownHostException uhe) {
            System.err.println("UnknownHostException: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

}

