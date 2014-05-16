package ist.cmov.proj.bomberboy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;

import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Types;
import ist.cmov.proj.bomberboy.utils.SettingsReader;

/**
 * Created by agfrg on 09/04/14.
 */
public class BomberView extends SurfaceView implements SurfaceHolder.Callback {
    private BomberThread thread;
    private boolean running = false;
    private boolean started = false;
    private boolean scaled = false;
    private long timeLeft;
    private int SIZE = 1064;
    protected Main main;

    public class BomberThread extends Thread {
        Object lock = new Object();
        boolean run = false;
        boolean redraw = true;
        SurfaceHolder holder;
        GameStatus status;
        Context context;
        HashMap<Types, Bitmap> bitmaps;
        float bitSize;
        int size;

        public BomberThread(SurfaceHolder h, Context c, float bS, int s, GameStatus ss) {
            holder = h;
            context = c;
            bitSize = bS;
            size = s;
            status = ss;

            bitmaps = new HashMap<Types, Bitmap>();
            bitmaps.put(Types.BARRIER, BitmapFactory.decodeResource(getResources(), R.drawable.barrier));
            bitmaps.put(Types.BOMB, BitmapFactory.decodeResource(getResources(), R.drawable.bomb));
            bitmaps.put(Types.PERSON, BitmapFactory.decodeResource(getResources(), R.drawable.person));
            bitmaps.put(Types.PERSONANDBOMB, bitmaps.get(Types.PERSON));
            bitmaps.put(Types.ROBOT, BitmapFactory.decodeResource(getResources(), R.drawable.robot));
            bitmaps.put(Types.ROBOTANDBOMB, bitmaps.get(Types.ROBOT));
            bitmaps.put(Types.WALL, BitmapFactory.decodeResource(getResources(), R.drawable.wall));
            bitmaps.put(Types.NULL, BitmapFactory.decodeResource(getResources(), R.drawable.grass));
            bitmaps.put(Types.EXPLOSION, BitmapFactory.decodeResource(getResources(), R.drawable.explosion));
            bitmaps.put(Types.EXPLOSIONANDBOMB, bitmaps.get(Types.EXPLOSION));

            status.addBomberThread(this);
        }

        public void setRunning(boolean b) {
            synchronized (lock) {
                run = b;
            }
        }

        private void setBitmap(Types[][] t, Canvas canvas) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    drawOnCanvas(t[i][j], canvas, i, j);
                }
            }
        }

        private void drawOnCanvas(Types t, Canvas c, Integer x, Integer y) {
            Bitmap b = bitmaps.get(t);
            RectF rect = new RectF(bitSize * y, bitSize * x, bitSize * y + bitSize, bitSize * x + bitSize);
            c.drawBitmap(b, null, rect, null);
            c.save();
        }

        /**
         * Scales the contents of the BomberView SurfaceView to the size of the device based on a
         * fixed, hard-coded, size written in the ui activity xml layout file.
         * <p/>
         * Source code from: http://stackoverflow.com/questions/10707519/scaling-a-fixed-surfaceview-to-fill-vertically-and-maintain-aspect-ratio
         */
        public void doDraw(Canvas c) {
            final float scaleFactor = Math.min(getWidth() / 1064.f, getHeight() / 1064.f);
            final float finalWidth = 1064.f * scaleFactor;
            final float finalHeight = 1064.f * scaleFactor;
            final float leftPadding = (getWidth() - finalWidth) / 2;
            final float topPadding = (getHeight() - finalHeight) / 2;

            final int savedState = c.save();
            try {
                c.clipRect(leftPadding, topPadding, leftPadding + finalWidth, topPadding + finalHeight);

                c.translate(leftPadding, topPadding);
                c.scale(scaleFactor, scaleFactor);
                setBitmap(status.getMap(), c);
            } finally {
                c.restoreToCount(savedState);
            }
        }

        public void signalRedraw() {
            redraw = true;
        }

        public BomberThread getThis() {
            return this;
        }

        public void smellyDied() {
            //status.endGame();
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(main, AlertDialog.THEME_HOLO_LIGHT);
                    alert.setCancelable(false);
                    alert.setTitle("You died!");

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });

                    alert.show();
                }
            });
        }

        public void updateClock(long millis) {
            timeLeft = millis;
            long seconds = (millis / 1000) % 60;
            long minutes = ((millis - seconds) / 1000) / 60;
            String time = minutes + ":" + seconds;

            main.updateClock(time);
        }

        public void decreaseTime() {
            updateClock(timeLeft - 1000);
        }

        public void gameEnds() {
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(main, AlertDialog.THEME_HOLO_LIGHT);
                    alert.setCancelable(false);
                    alert.setTitle("The game ends!");

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            status = new GameStatus();
                            status.initializeGameStatus(SettingsReader.getSettings());
                            status.addBomberThread(getThis());
                            main.setGameStatus(status);
                            main.getPlayer();
                            status.beginGame();
                        }
                    });

                    alert.show();
                }
            });
        }

        @Override
        public void run() {
            while (run) {
                Canvas c = null;
                try {
                    c = holder.lockCanvas(null);
                    synchronized (holder) {
                        synchronized (lock) {
                            doDraw(c);
                        }
                    }
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }

    public BomberView(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void startThread(Context context, int s, GameStatus ss, Main main) {
        this.main = main;
        if (thread == null)
            thread = new BomberThread(getHolder(), context, SIZE / s, s, ss);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread.setRunning(true);
        if (!started) thread.start();
        started = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public void signalRedraw() {
        thread.signalRedraw();
    }

    public void toggleRunning() {
        running = !running;
        thread.setRunning(running);
    }

    @Override
    public void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        SIZE = xNew;
    }
}
