package Sudoku;

import java.util.*;

public class Utils {
    final static int DEFAULT_MASTER_PORT = 4000;
    final static int NUMBER_EMPTY_CELLS = 4;
    final static String GAME_FINISHED = "MESSAGE_GAME_FINISHED";

    public static <String, Player extends Comparable<? super Player>> LinkedHashMap<String, Player> sortPlayers(HashMap<String, Player> map) {
        List<Map.Entry<String, Player>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        LinkedHashMap<String, Player> result = new LinkedHashMap<>();
        for (Map.Entry<String, Player> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
