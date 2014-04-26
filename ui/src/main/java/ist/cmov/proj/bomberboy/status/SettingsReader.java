package ist.cmov.proj.bomberboy.status;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import ist.cmov.proj.bomberboy.control.robots.Robot;

public class SettingsReader {
    private static Types[][] map;
    private static ArrayList<Robot> robots = new ArrayList<Robot>();

    public static Types[][] getMap() {
        return map;
    }

    public static ArrayList<Robot> getRobots() {
        return robots;
    }

    public static void readSettings(BufferedReader reader, GameStatus status) {
        ArrayList<String> map = new ArrayList<String>();
        try {
            String s = reader.readLine();
            while (s != null) {
                map.add(s);
                s = reader.readLine();
            }
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }

        SettingsReader.map = new Types[status.SIZE][status.SIZE];

        for(int i = 0; i < map.size(); i++) {
            parseString(map.get(i), SettingsReader.map[i], status, i);
        }

        status.initializeGameStatus(SettingsReader.map, SettingsReader.robots);
    }

    private static void parseString(String l, Types[] typeMap, GameStatus s, int x) {
        for (int y = 0; y < l.length(); y++) {
            char p = l.charAt(y);
            if (p == '-') {
                typeMap[y] = Types.NULL;
            } else if (p == 'W') {
                typeMap[y] = Types.WALL;
            } else if (p == 'O') {
                typeMap[y] = Types.BARRIER;
            } else if (p == 'R') {
                typeMap[y] = Types.ROBOT;
                robots.add(new Robot(s, x, y));
            }
        }
    }
}
