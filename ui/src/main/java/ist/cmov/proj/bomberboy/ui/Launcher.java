package ist.cmov.proj.bomberboy.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.wifidirect.*;


public class Launcher extends Activity implements PlayerListFragment.DeviceActionListener, WifiP2pManager.ConnectionInfoListener {

    public static final String TAG = "Launcher";

    private boolean isWifiP2pEnabled = false;


    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

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

    private View.OnClickListener listenerStartGameButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getApplicationContext(), Main.class);
            startActivity(i);
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
            if (!isWifiP2pEnabled) {
                Toast.makeText(getApplicationContext(), R.string.p2p_off_warning,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            final PlayerListFragment fragment = (PlayerListFragment) getFragmentManager()
                    .findFragmentById(R.id.player_list_frag);
            fragment.onInitiateDiscovery();
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Refresh Started...", Toast.LENGTH_SHORT).show();
                    Log.d(Launcher.TAG, "Refresh Started...");
                }

                @Override
                public void onFailure(int errorCode) {
                    Toast.makeText(getApplicationContext(), "Refresh failed. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.d(Launcher.TAG, "Refresh Failed because of : " + errorCode);
                }
            });
        }
    };


    private void guiSetButtonListeners() {
        findViewById(R.id.idDisconnectButton).setOnClickListener(listenerDisconnectButton);
        findViewById(R.id.idRefreshPeersButton).setOnClickListener(listenerRefreshButton);
        findViewById(R.id.idStartGameButton).setOnClickListener(listenerStartGameButton);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo structure.
        InetAddress groupOwnerAddress = info.groupOwnerAddress;

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            GameStatus.SERVER_MODE = true;
            GameStatus.info = info;
            Toast.makeText(getApplicationContext(), "I'm the group owner!", Toast.LENGTH_SHORT).show();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            GameStatus.SERVER_MODE = false;
            GameStatus.info = info;
            Toast.makeText(getApplicationContext(), "I'm just a peer", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Success.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void resetData() {
        PlayerListFragment fragmentList = (PlayerListFragment) getFragmentManager()
                .findFragmentById(R.id.player_list_frag);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
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
