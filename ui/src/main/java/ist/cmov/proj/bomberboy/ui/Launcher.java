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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import ist.cmov.proj.bomberboy.wifidirect.WifiDirectBroadcastReceiver;


public class Launcher extends Activity {

    public static final String TAG = "Launcher";
    public static final int PORT = 8888;
    private List peers = new ArrayList();
    private WifiP2pGroup p2pGroup;
    private WifiP2pInfo p2pInfo;
    private Socket goConnection;

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
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                    p2pGroup = wifiP2pGroup;
                }
            });
            if (p2pGroup == null) {
                Toast.makeText(getApplicationContext(), "Failed to get Group!", Toast.LENGTH_SHORT).show();
                findViewById(R.id.idConnectButton).setEnabled(true);
                return;
            }
            WifiP2pDevice go = p2pGroup.getOwner();
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = go.deviceAddress;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    String result = "Connected to the group owner";
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            });

            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                    p2pInfo = wifiP2pInfo;
                }
            });

            InetAddress inet = p2pInfo.groupOwnerAddress;
            goConnection = new Socket();
            try {
                goConnection.bind(null);
                goConnection.connect(new InetSocketAddress(inet, PORT));
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
    };

    private View.OnClickListener listenerDisconnectButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            findViewById(R.id.idDisconnectButton).setEnabled(false);
        }
    };

    private View.OnClickListener listenerCreateGroupButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            findViewById(R.id.idCreateGroup).setEnabled(false);
            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int i) {

                }
            });
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
        findViewById(R.id.idCreateGroup).setOnClickListener(listenerCreateGroupButton);
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
