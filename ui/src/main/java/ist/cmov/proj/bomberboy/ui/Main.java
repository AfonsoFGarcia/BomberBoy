package ist.cmov.proj.bomberboy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.server.IncomingRequest;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;
import ist.cmov.proj.bomberboy.utils.NoSuchTypeException;
import ist.cmov.proj.bomberboy.utils.SettingsReader;

public class Main extends Activity {

    public static BomberView game;
    protected boolean scalingComplete = false;
    protected String playerName = null;
    public static GameStatus g;
    IncomingRequest socketWrapper;
    protected Player me;
    private int _port = 4444;

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
                    g.register(playerName, me.getX(), me.getY());
                    g.beginGame();
                }
            }
        });

        alert.show();
    }

    public void setGameStatus(GameStatus g) {
        this.g = g;
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.toggleRunning();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void getPlayer() {
        me = g.getPlayer();
        setPoints(me.getScore().toString());
    }

    public void setPoints(String playerScore) {
        TextView player = (TextView) findViewById(R.id.playerScore);
        player.setText(playerScore);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        game = (BomberView) findViewById(R.id.gameView);

        g = new GameStatus();

        BufferedReader l = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.l1)));

        try {
            SettingsReader.readSettings(l, g, this);
        } catch (NoSuchTypeException e) {
            System.err.println(e.getClass().getCanonicalName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        g.initializeSettings();

        socketWrapper = new IncomingRequest(_port);
        socketWrapper.start();
        getPlayer();

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

        getName();

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

    @SuppressWarnings("deprecation")
    public static void scaleViewAndChildren(View root, float scale) {
        // Retrieve the view's layout information
        ViewGroup.LayoutParams layoutParams = root.getLayoutParams();


        // Scale the view itself
        assert layoutParams != null;
        if (layoutParams.width != ViewGroup.LayoutParams.FILL_PARENT && layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            layoutParams.width *= scale;
        }
        if (layoutParams.height != ViewGroup.LayoutParams.FILL_PARENT && layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
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

    @Override
    protected void onPause() {
        super.onPause();
        game.toggleRunning();
        game.invalidate();
    }
}
