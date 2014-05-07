package ist.cmov.proj.bomberboy.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ist.cmov.proj.bomberboy.wifidirect.WifiDirectBroadcastReceiver;


public class Launcher extends Activity {

    public static final String TAG = "Launcher";
    private List peers = new ArrayList();

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        guiSetButtonListeners();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    public WifiP2pManager.PeerListListener peerListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList deviceList) {
            peers.clear();
            peers.addAll(deviceList.getDeviceList());
        }
    };

    private View.OnClickListener listenerConnectButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            findViewById(R.id.idConnectButton).setEnabled(false);
        }
    };

    private View.OnClickListener listenerDisconnectButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            findViewById(R.id.idDisconnectButton).setEnabled(false);
        }
    };

    private View.OnClickListener listenerRefreshButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(Launcher.TAG, "Refresh Completed.");
                }

                @Override
                public void onFailure(int i) {

                }
            });
        }
    };

    private void guiSetButtonListeners() {
        findViewById(R.id.idConnectButton).setOnClickListener(listenerConnectButton);
        findViewById(R.id.idDisconnectButton).setOnClickListener(listenerDisconnectButton);
        findViewById(R.id.idRefreshPeersButton).setOnClickListener(listenerRefreshButton);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bomber_boy_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
