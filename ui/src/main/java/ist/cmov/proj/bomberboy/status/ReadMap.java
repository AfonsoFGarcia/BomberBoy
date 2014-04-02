package ist.cmov.proj.bomberboy.status;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by agfrg on 02/04/14.
 */
public class ReadMap {
    public static ArrayList<ArrayList<Types>> getMap(BufferedReader reader) {
        ArrayList<String> map = new ArrayList<String>();
        try {
            String s = reader.readLine();
            while (s != null) {
                map.add(s);
                s = reader.readLine();
            }
        } catch (IOException e) {
        }

        ArrayList<ArrayList<Types>> typeMap = new ArrayList<ArrayList<Types>>(GameStatus.SIZE);

        for(int i = 0; i < map.size(); i++) {
            parseString(map.get(i), typeMap, i);
        }
        return typeMap;
    }

    private static void parseString(String l, ArrayList<ArrayList<Types>> typeMap, int index) {
        typeMap.add(index, new ArrayList<Types>(GameStatus.SIZE));
        for(int i = 0; i < l.length(); i++) {
            char p = l.charAt(i);
            if (p == '-') {
                typeMap.get(index).add(i, Types.NULL);
            } else if (p == 'W') {
                typeMap.get(index).add(i, Types.WALL);
            } else if (p == 'O') {
                typeMap.get(index).add(i, Types.BARRIER);
            } else if (p == 'R') {
                typeMap.get(index).add(i, Types.ROBOT);
            }
        }
    }
}
