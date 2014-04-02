package ist.cmov.proj.bomberboy.status;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.HashMap;

import ist.cmov.proj.bomberboy.ui.R;

public class GameStatus {

    protected HashMap<Types, Bitmap> bitmaps;
    public static int SIZE = 19;
    private ArrayList<ArrayList<Types>> t;
    private Pair p = null;

    private void readBitmaps(Context c) {
        bitmaps = new HashMap<Types, Bitmap>();
        bitmaps.put(Types.BARRIER, BitmapFactory.decodeResource(c.getResources(), R.drawable.barrier));
        bitmaps.put(Types.BOMB, BitmapFactory.decodeResource(c.getResources(), R.drawable.bomb));
        bitmaps.put(Types.PERSON, BitmapFactory.decodeResource(c.getResources(), R.drawable.person));
        bitmaps.put(Types.WALL, BitmapFactory.decodeResource(c.getResources(), R.drawable.wall));
        bitmaps.put(Types.NULL, BitmapFactory.decodeResource(c.getResources(), R.drawable.grass));
    }

    private void createPlayer() {
        t.get(1).remove(1);
        t.get(1).add(1, Types.PERSON);
        p = new Pair(1, 1);
    }

    public GameStatus(Context c, ArrayList<ArrayList<Types>> types) {
        readBitmaps(c);
        t = types;
        createPlayer();
    }

    private boolean canMove(Movements e) {
        if (e.equals(Movements.DOWN) && p.x < SIZE - 1 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.UP) && p.x > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.LEFT) && p.y > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && p.y < SIZE - 1 && isNotOccupied(e)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNotOccupied(Movements e) {
        if (e.equals(Movements.DOWN) && t.get(p.x + 1).get(p.y).equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.UP) && t.get(p.x - 1).get(p.y).equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.LEFT) && t.get(p.x).get(p.y - 1).equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && t.get(p.x).get(p.y + 1).equals(Types.NULL)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean move(Movements e) {
        if (!canMove(e)) return false;
        t.get(p.x).set(p.y, Types.NULL);
        if (e.equals(Movements.DOWN)) {
            p.incrX();
        } else if (e.equals(Movements.UP)) {
            p.decrX();
        } else if (e.equals(Movements.LEFT)) {
            p.decrY();
        } else {
            p.incrY();
        }
        t.get(p.x).set(p.y, Types.PERSON);
        return true;
    }

    public Bitmap getBitmap() {
        Bitmap bg = Bitmap.createBitmap(475, 475, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        /* Represents a line equation that gets the size of the bitmap in the canvas in order to the size of the board */
        int bitSize = (25*(SIZE)-450);

        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                drawOnCanvas(t.get(i).get(j), canvas, i, j, bitSize);
            }
        }

        return bg;
    }

    private void drawOnCanvas(Types t, Canvas c, Integer x, Integer y, Integer bitSize) {
        Bitmap b = bitmaps.get(t);
        Rect rect = new Rect(bitSize * y, bitSize * x, bitSize * y + bitSize, bitSize * x + bitSize);
        c.drawBitmap(b, null, rect, null);
    }
}
