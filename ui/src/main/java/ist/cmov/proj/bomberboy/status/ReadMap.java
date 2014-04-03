package ist.cmov.proj.bomberboy.status;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadMap {
    public static Types[][] getMap(BufferedReader reader) {
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

        Types[][] typeMap = new Types[GameStatus.SIZE][GameStatus.SIZE];

        for(int i = 0; i < map.size(); i++) {
            parseString(map.get(i), typeMap[i]);
        }
        return typeMap;
    }

    private static void parseString(String l, Types[] typeMap) {
        for(int i = 0; i < l.length(); i++) {
            char p = l.charAt(i);
            if (p == '-') {
                typeMap[i] = Types.NULL;
            } else if (p == 'W') {
                typeMap[i] = Types.WALL;
            } else if (p == 'O') {
                typeMap[i] = Types.BARRIER;
            } else if (p == 'R') {
                typeMap[i] = Types.ROBOT;
            }
        }
    }
}
