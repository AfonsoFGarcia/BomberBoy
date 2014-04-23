package ist.cmov.proj.bomberboy.status;

import android.os.AsyncTask;

import ist.cmov.proj.bomberboy.ui.BomberView;

public class GameStatus {

    public static int SIZE = 19;
    private static int RANGE = 8;
    protected static int TIME = 5000;
    protected Object lock = new Object();
    private Types[][] t;
    private Pair p = null;
    BomberView.BomberThread thread;

    public void addBomberThread(BomberView.BomberThread t) {
        thread = t;
    }

    public Types[][] getMap() {
        return t;
    }

    private void createPlayer() {
        t[1][1] = Types.PERSON;
        p = new Pair(1, 1);
    }

    public GameStatus(Types[][] types) {
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
        if (e.equals(Movements.DOWN) && emptyPosition(p.x + 1, p.y)) {
            return true;
        } else if (e.equals(Movements.UP) && emptyPosition(p.x - 1, p.y)) {
            return true;
        } else if (e.equals(Movements.LEFT) && emptyPosition(p.x, p.y - 1)) {
            return true;
        } else if (e.equals(Movements.RIGHT) && emptyPosition(p.x, p.y + 1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean emptyPosition(int x, int y) {
        return t[x][y].equals(Types.NULL);
    }

    public boolean move(Movements e) {
        synchronized (lock) {
            if (!canMove(e)) return false;

            if (t[p.x][p.y].equals(Types.PERSONANDBOMB)) {
                t[p.x][p.y] = Types.BOMB;
            } else {
                t[p.x][p.y] = Types.NULL;
            }

            if (e.equals(Movements.DOWN)) {
                p.incrX();
            } else if (e.equals(Movements.UP)) {
                p.decrX();
            } else if (e.equals(Movements.LEFT)) {
                p.decrY();
            } else {
                p.incrY();
            }

            if (t[p.x][p.y].equals(Types.BOMB)) {
                t[p.x][p.y] = Types.PERSONANDBOMB;
            } else {
                t[p.x][p.y] = Types.PERSON;
            }

            return true;
        }
    }

    public boolean dropBomb() {
        synchronized (lock) {
            if (!p.hasBomb()) {
                t[p.x][p.y] = Types.PERSONANDBOMB;
                p.toggleBomb();

                BlowBombTask task = new BlowBombTask();
                task.execute(p.x, p.y);

                return true;
            } else {
                return false;
            }
        }
    }

    private class BlowBombTask extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {
            try {
                Thread.sleep(TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock) {
                t[params[0]][params[1]] = Types.NULL;
                p.toggleBomb();
                if (cleanTab(params[0], params[1])) {
                    thread.smellyDied();
                }
            }

            return null;
        }

        private boolean killSmelly(int x, int y) {
            if (p.x == x && p.y == y) {
                p.kill();
                return true;
            }
            return false;
        }

        private boolean cleanTab(int x, int y) {
            boolean returnValue = false;
            for (int i = 0; i < RANGE; i++) {
                if (y + i < SIZE && !t[x][y + i].equals(Types.WALL)) {
                    returnValue = returnValue || killSmelly(x, y + i);
                    t[x][y + i] = Types.NULL;
                } else {
                    break;
                }
            }
            for (int i = 0; i < RANGE; i++) {
                if (x + i < SIZE && !t[x + i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killSmelly(x + i, y);
                    t[x + i][y] = Types.NULL;
                } else {
                    break;
                }
            }
            for (int i = 0; i < RANGE; i++) {
                if (y - i >= 0 && !t[x][y - i].equals(Types.WALL)) {
                    returnValue = returnValue || killSmelly(x, y - i);
                    t[x][y - i] = Types.NULL;
                } else {
                    break;
                }
            }
            for (int i = 0; i < RANGE; i++) {
                if (x - i >= 0 && !t[x - i][y].equals(Types.WALL)) {
                    returnValue = returnValue || killSmelly(x - i, y);
                    t[x - i][y] = Types.NULL;
                } else {
                    break;
                }
            }
            return returnValue;
        }
    }
}
