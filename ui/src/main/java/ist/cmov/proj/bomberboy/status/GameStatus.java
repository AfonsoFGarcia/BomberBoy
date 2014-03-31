package ist.cmov.proj.bomberboy.status;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import ist.cmov.proj.bomberboy.ui.R;

public class GameStatus {

    private Types[][] t = null;
    private Integer LIMIT = null;
    private Pair p = null;

    public GameStatus() {
        Integer size = 20;
        t = new Types[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                t[i][j] = Types.NULL;
            }
        }
        LIMIT = size - 1;
        setWalls();
        p = new Pair(1, 1);
        t[1][1] = Types.PERSON;
    }

    private void setWalls() {
        for (int i = 0; i <= LIMIT; i++) {
            t[i][0] = Types.WALL;
            t[i][LIMIT] = Types.WALL;
        }
        for (int i = 0; i <= LIMIT; i++) {
            t[0][i] = Types.WALL;
            t[LIMIT][i] = Types.WALL;
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
        if (e.equals(Movements.DOWN) && t[p.x + 1][p.y].equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.UP) && t[p.x - 1][p.y].equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.LEFT) && t[p.x][p.y - 1].equals(Types.NULL)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && t[p.x][p.y + 1].equals(Types.NULL)) {
            return true;
        } else {
            return false;
        }
    }

    public void move(Movements e) {
        if (e.equals(Movements.DOWN)) {
            t[p.x][p.y] = Types.NULL;
            p.incrX();
            t[p.x][p.y] = Types.PERSON;
        } else if (e.equals(Movements.UP)) {
            t[p.x][p.y] = Types.NULL;
            p.decrX();
            t[p.x][p.y] = Types.PERSON;
        } else if (e.equals(Movements.LEFT)) {
            t[p.x][p.y] = Types.NULL;
            p.decrY();
            t[p.x][p.y] = Types.PERSON;
        } else {
            t[p.x][p.y] = Types.NULL;
            p.incrY();
            t[p.x][p.y] = Types.PERSON;
        }
    }

    public Bitmap getBitmap(Context c) {
        Bitmap bg = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        int bitSize = (150-5*(LIMIT+1))/2;

        for(int i = 0; i <= LIMIT; i++) {
            for(int j = 0; j <= LIMIT; j++) {
                drawOnCanvas(t[i][j], canvas, i, j, bitSize, c);
            }
        }

        return bg;
    }

    private void drawOnCanvas(Types t, Canvas c, Integer x, Integer y, Integer bitSize, Context cont) {
        Bitmap b = null;
        if (t.equals(Types.BARRIER)) {
            b = BitmapFactory.decodeResource(cont.getResources(), R.drawable.barrier);
        } else if (t.equals(Types.BOMB)) {
            b = BitmapFactory.decodeResource(cont.getResources(), R.drawable.bomb);
        } else if (t.equals(Types.PERSON)) {
            b = BitmapFactory.decodeResource(cont.getResources(), R.drawable.person);
        } else if (t.equals(Types.WALL)) {
            b = BitmapFactory.decodeResource(cont.getResources(), R.drawable.wall);
        } else {
            b = BitmapFactory.decodeResource(cont.getResources(), R.drawable.grass);
        }
        Rect rect = new Rect(bitSize * y, bitSize * x, bitSize * y + bitSize, bitSize * x + bitSize);
        c.drawBitmap(b, null, rect, null);
    }
}
