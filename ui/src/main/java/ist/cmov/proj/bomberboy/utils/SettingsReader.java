package ist.cmov.proj.bomberboy.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import ist.cmov.proj.bomberboy.control.players.Player;
import ist.cmov.proj.bomberboy.control.robots.Robot;
import ist.cmov.proj.bomberboy.status.GameStatus;
import ist.cmov.proj.bomberboy.status.Types;
import ist.cmov.proj.bomberboy.ui.Main;

public class SettingsReader {
    private static ArrayList<Robot> robots;
    private static Stack<Player> players;
    private static GameSettings settings;

    public static GameSettings getSettings() {
        return settings;
    }

    public static void readSettings(BufferedReader reader, GameStatus status, Main main) throws NoSuchTypeException {

        robots = new ArrayList<Robot>();
        players = new Stack<Player>();

        ArrayList<String> mapStrings = new ArrayList<String>();
        try {
            String s = reader.readLine();
            while (s != null) {
                mapStrings.add(s);
                s = reader.readLine();
            }
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }

        Types[][] map = new Types[status.SIZE][status.SIZE];

        for (int i = 9; i < mapStrings.size(); i++) {
            parseString(mapStrings.get(i), map[i - 9], status, i - 9, main);
        }

        SettingsReader.settings = new GameSettings(mapStrings.get(0),
                Integer.parseInt(mapStrings.get(1).substring(3)),
                Integer.parseInt(mapStrings.get(2).substring(3)),
                Integer.parseInt(mapStrings.get(3).substring(3)),
                Integer.parseInt(mapStrings.get(4).substring(3)),
                Double.parseDouble(mapStrings.get(5).substring(3)),
                Integer.parseInt(mapStrings.get(6).substring(3)),
                Integer.parseInt(mapStrings.get(7).substring(3)),
                map, robots, players);

        status.initializeGameStatus(settings);
    }

    private static void parseString(String l, Types[] typeMap, GameStatus s, int x, Main main) throws NoSuchTypeException {
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
            } else if (p == 'P') {
                typeMap[y] = Types.NULL;
                players.push(new Player(x, y, s, main));
            } else {
                throw new NoSuchTypeException(p);
            }
        }
    }
}
