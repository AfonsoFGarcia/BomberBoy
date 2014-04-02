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
    private Integer LIMIT = SIZE - 1;
    private Pair p = null;

    private GameStatus(Context c, Integer s) {
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
        this(c, SIZE);
        t = types;
        createPlayer();
    }

    public GameStatus(Context c) {
        this(c, SIZE);
        emptyMap();
        createPlayer();
    }

    private void emptyMap() {
        t = new ArrayList<ArrayList<Types>>();
        for (int i = 0; i < SIZE; i++) {
            t.add(i, new ArrayList<Types>());
            for (int j = 0; j < SIZE; j++) {
                if(i != 1 && j != 1) t.get(i).add(j, Types.NULL);
            }
        }
        setWalls();
    }

    private void setWalls() {
        for (int i = 0; i <= LIMIT; i++) {
            t.get(i).add(0, Types.WALL);
            t.get(0).add(i, Types.WALL);
            t.get(i).add(LIMIT, Types.WALL);
            t.get(LIMIT).add(i, Types.WALL);
        }
    }

    public boolean canMove(Movements e) {
        if (e.equals(Movements.DOWN) && p.x < LIMIT && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.UP) && p.x > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.LEFT) && p.y > 0 && isNotOccupied(e)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && p.y < LIMIT && isNotOccupied(e)) {
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

    public void move(Movements e) {
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
    }

    public Bitmap getBitmap() {
        Bitmap bg = Bitmap.createBitmap(475, 475, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        int bitSize = (25*(LIMIT+1)-450);

        for(int i = 0; i <= LIMIT; i++) {
            for(int j = 0; j <= LIMIT; j++) {
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
