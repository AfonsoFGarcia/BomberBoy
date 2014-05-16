package ist.cmov.proj.bomberboy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.conn.ClientConnectionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.Buffer;

import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;
import ist.cmov.proj.bomberboy.utils.NoSuchTypeException;
import ist.cmov.proj.bomberboy.utils.SettingsReader;
import ist.cmov.proj.bomberboy.wifidirect.service.ClientService;
import ist.cmov.proj.bomberboy.wifidirect.service.ServerService;

public class Main extends Activity {

    public static final String TAG = "Main";
    public static BomberView game;
    protected boolean scalingComplete = false;
    protected String playerName = null;
    public static GameStatus g;
    protected Player me = null;
    ServerService mServer;
    ClientService mClient;
    boolean mBoundServer = false;
    boolean mBoundClient = false;
    Intent i;

    private void getName() {
        getName("Enter your name");
    }

    public void updateClock(String time) {
        final String finalTime = time;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView player = (TextView) findViewById(R.id.timeLeft);
                player.setText(finalTime);
            }
        });
    }

    private void getName(String title) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alert.setTitle(title);
        alert.setCancelable(false);

        AlertDialog dialog;
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //noinspection ConstantConditions
                playerName = input.getText().toString();
                if (playerName.equals("")) {
                    getName("Enter a valid name");
                } else {
                    TextView player = (TextView) findViewById(R.id.playerName);
                    player.setText(playerName);
                    register(playerName);
                    while (me == null) {
                    }
                    setPoints(me.getScore().toString());
                    g.beginGame();
                    dialog.dismiss();
                }
            }
        });
        dialog = alert.create();
        dialog.show();
    }

    public void setGameStatus(GameStatus g) {
        this.g = g;
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.toggleRunning();
    }

    public void register(String name) {
        g.register(name, this);
    }

    public void createPlayer(Player p) {
        me = p;
    }

    public void getPlayer() {
        getName();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPoints(String playerScore) {
        TextView player = (TextView) findViewById(R.id.playerScore);
        player.setText(playerScore);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Integer level = getIntent().getExtras().getInt("LEVEL", R.raw.l1);
        setContentView(R.layout.activity_main);
        game = (BomberView) findViewById(R.id.gameView);

        g = new GameStatus();
        BufferedReader l = null;
        if (level == R.raw.l1)
            l = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.l1)));
        if (level == R.raw.l1_demo)
            l = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.l1_demo)));
        if (level == R.raw.l2_demo)
            l = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.l2_demo)));
        if (level == R.raw.l3_demo)
            l = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.l3_demo)));

        try {
            SettingsReader.readSettings(l, g, this);
        } catch (NoSuchTypeException e) {
            System.err.println(e.getClass().getCanonicalName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        g.initializeSettings();

        game.startThread(getApplicationContext(), GameStatus.SIZE, g, this);

        final Button button_a = (Button) findViewById(R.id.button_a);
        button_a.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (me.dropBomb()) {
                    game.signalRedraw();
                }
            }
        });

        final Button button_b = (Button) findViewById(R.id.button_b);
        button_b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                game.signalRedraw();
            }
        });

        final Button button_u = (Button) findViewById(R.id.button_u);
        button_u.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (me.move(Movements.UP)) {
                    game.signalRedraw();
                }
            }
        });

        final Button button_d = (Button) findViewById(R.id.button_d);
        button_d.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (me.move(Movements.DOWN)) {
                    game.signalRedraw();
                }
            }
        });

        final Button button_l = (Button) findViewById(R.id.button_l);
        button_l.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (me.move(Movements.LEFT)) {
                    game.signalRedraw();
                }
            }
        });

        final Button button_r = (Button) findViewById(R.id.button_r);
        button_r.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (me.move(Movements.RIGHT)) {
                    game.signalRedraw();
                }
            }
        });

        if (GameStatus.SERVER_MODE) {
            i = new Intent(getApplicationContext(), ServerService.class);
            bindService(i, mConnectionServer, Context.BIND_AUTO_CREATE);
        } else {
            i = new Intent(getApplicationContext(), ClientService.class);
            bindService(i, mConnectionClient, Context.BIND_AUTO_CREATE);
        }
        getPlayer();

    }

    public void setPlayerCount(int count) {
        TextView text = (TextView) findViewById(R.id.numberPlayers);
        text.setText(new Integer(count).toString());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        game.signalRedraw();
        if (!scalingComplete) // only do this once
        {
            scaleContents(findViewById(R.id.window_nexus5), findViewById(R.id.container));
            scalingComplete = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    /**
     * Scales the contents of the given view so that it completely fills the given
     * container on one axis (that is, we're scaling isotropically).
     * <p/>
     * Source code used from paper http://www.vanteon.com/downloads/Scaling_Android_Apps_White_Paper.pdf
     */
    private void scaleContents(View rootView, View container) {
        // Compute the scaling ratio
        float xScale = (float) container.getWidth() / rootView.getWidth();
        float yScale = (float) container.getHeight() / rootView.getHeight();
        float scale = Math.min(xScale, yScale);

        // Scale our contents
        scaleViewAndChildren(rootView, scale);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, Launcher.class);
        startActivity(i);
    }

    public static void scaleViewAndChildren(View root, float scale) {
        // Retrieve the view's layout information
        ViewGroup.LayoutParams layoutParams = root.getLayoutParams();


        // Scale the view itself
        assert layoutParams != null;
        if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            layoutParams.width *= scale;
        }
        if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            layoutParams.height *= scale;
        }

        // If this view has margins, scale those too
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginParams.leftMargin *= scale;
            marginParams.rightMargin *= scale;
            marginParams.topMargin *= scale;
            marginParams.bottomMargin *= scale;
        }

        // Set the layout information back into the view
        root.setLayoutParams(layoutParams);

        // Scale the view's padding
        root.setPadding((int) (root.getPaddingLeft() * scale), (int) (root.getPaddingTop() * scale), (int) (root.getPaddingRight() * scale), (int) (root.getPaddingBottom() * scale));

        // If the root view is a ViewGroup, scale all of its children recursively
        if (root instanceof ViewGroup) {
            ViewGroup groupView = (ViewGroup) root;
            for (int cnt = 0; cnt < groupView.getChildCount(); ++cnt) {
                scaleViewAndChildren(groupView.getChildAt(cnt), scale);
            }
        }
    }

    private ServiceConnection mConnectionServer = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ServerService.ServerBinder binder = (ServerService.ServerBinder) service;
            mServer = binder.getService();
            mBoundServer = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundServer = false;
        }
    };

    private ServiceConnection mConnectionClient = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ClientService.ClientBinder binder = (ClientService.ClientBinder) iBinder;
            mClient = binder.getService();
            mBoundClient = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundClient = false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        game.toggleRunning();
        game.invalidate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBoundServer) {
            unbindService(mConnectionServer);
            stopService(i);
            mBoundServer = false;
        }
        if (mBoundClient) {
            unbindService(mConnectionClient);
            stopService(i);
            mBoundClient = false;
        }
        g.stop();
    }
}
