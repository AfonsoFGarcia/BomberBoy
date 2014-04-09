package ist.cmov.proj.bomberboy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Movements;
import ist.cmov.proj.bomberboy.status.ReadMap;
import ist.cmov.proj.bomberboy.status.Types;

public class Main extends Activity {

    private SurfaceView game;
    private boolean scalingComplete = false;
    private String playerName;
    private GameStatus g;
    private Bitmap bg = Bitmap.createBitmap(475, 475, Bitmap.Config.ARGB_8888);
    private Canvas canvas = new Canvas(bg);
    protected HashMap<Types, Bitmap> bitmaps;
    private static int SIZE;
    private int bitSize;

    @SuppressWarnings("deprecation")
    private void draw() {
        setBitmap(g.getMap());
        game.setBackgroundDrawable(new BitmapDrawable(bg));
    }

    private void getName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);

        alert.setTitle("Enter your name");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //noinspection ConstantConditions
                playerName = input.getText().toString();
                if (playerName.equals("")) getName();
                TextView player = (TextView) findViewById(R.id.playerName);
                player.setText(playerName);
            }
        });

        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bitmaps = new HashMap<Types, Bitmap>();
        bitmaps.put(Types.BARRIER, BitmapFactory.decodeResource(getResources(), R.drawable.barrier));
        bitmaps.put(Types.BOMB, BitmapFactory.decodeResource(getResources(), R.drawable.bomb));
        bitmaps.put(Types.PERSON, BitmapFactory.decodeResource(getResources(), R.drawable.person));
        bitmaps.put(Types.WALL, BitmapFactory.decodeResource(getResources(), R.drawable.wall));
        bitmaps.put(Types.NULL, BitmapFactory.decodeResource(getResources(), R.drawable.grass));

        setContentView(R.layout.activity_main);
        game = (SurfaceView) findViewById(R.id.gameView);
        BufferedReader l = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.l1)));
        Types[][] m = ReadMap.getMap(l);
        g = new GameStatus(m);
        SIZE = GameStatus.SIZE;

        /* Represents a line equation that gets the size of the bitmap in the canvas in order to the size of the board */
        bitSize = (25 * (SIZE) - 450);

        setBitmap(g.getMap());
        draw();

        final Button button_a = (Button) findViewById(R.id.button_a);
        button_a.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                draw();
            }
        });

        final Button button_b = (Button) findViewById(R.id.button_b);
        button_b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                draw();
            }
        });

        final Button button_u = (Button) findViewById(R.id.button_u);
        button_u.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (g.move(Movements.UP, canvas)) {
                    draw();
                }
            }
        });

        final Button button_d = (Button) findViewById(R.id.button_d);
        button_d.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (g.move(Movements.DOWN, canvas)) {
                    draw();
                }
            }
        });

        final Button button_l = (Button) findViewById(R.id.button_l);
        button_l.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (g.move(Movements.LEFT, canvas)) {
                    draw();
                }
            }
        });

        final Button button_r = (Button) findViewById(R.id.button_r);
        button_r.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (g.move(Movements.RIGHT, canvas)) {
                    draw();
                }
            }
        });

        getName();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!scalingComplete) // only do this once
        {
            scaleContents(findViewById(R.id.window_nexus5), findViewById(R.id.container));
            scalingComplete = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    /** Scales the contents of the given view so that it completely fills the given
     *  container on one axis (that is, we're scaling isotropically).
     *
     *  Source code used from paper http://www.vanteon.com/downloads/Scaling_Android_Apps_White_Paper.pdf
     */
    private void scaleContents(View rootView, View container) {
        // Compute the scaling ratio
        float xScale = (float)container.getWidth() / rootView.getWidth();
        float yScale = (float)container.getHeight() / rootView.getHeight();
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
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)layoutParams;
            marginParams.leftMargin *= scale;
            marginParams.rightMargin *= scale;
            marginParams.topMargin *= scale;
            marginParams.bottomMargin *= scale;
        }

        // Set the layout information back into the view
        root.setLayoutParams(layoutParams);

        // Scale the view's padding
        root.setPadding((int)(root.getPaddingLeft() * scale), (int)(root.getPaddingTop() * scale), (int)(root.getPaddingRight() * scale), (int)(root.getPaddingBottom() * scale));

        // If the root view is a ViewGroup, scale all of its children recursively
        if (root instanceof ViewGroup) {
            ViewGroup groupView = (ViewGroup)root;
            for (int cnt = 0; cnt < groupView.getChildCount(); ++cnt) {
                scaleViewAndChildren(groupView.getChildAt(cnt), scale);
            }
        }
    }

    public void setBitmap(Types[][] t) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                drawOnCanvas(t[i][j], canvas, i, j);
            }
        }
    }

    private void drawOnCanvas(Types t, Canvas c, Integer x, Integer y) {
        Bitmap b = bitmaps.get(t);
        Rect rect = new Rect(bitSize * y, bitSize * x, bitSize * y + bitSize, bitSize * x + bitSize);
        c.drawBitmap(b, null, rect, null);
    }
}
