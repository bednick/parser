package main.java.ru.old.computationalModel.line.comments;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс хранящий в себе все доступные комментарии
 */
public class DownloadComment {
    private static Map<String, Comment> map;

    static {
        map = new HashMap<>();
        map.put("result", new ComResult());
        map.put("rubbish", new ComRubbish());
        map.put("notRubbish", new ComNotRubbish());
        map.put("mark", new ComCreateMark());
        map.put("time", new ComWeightTime());
        map.put("memory", new ComWeightMemory());
    }

    public static Comment getComment(String comment) {
        if (map.containsKey(comment)) {
            return map.get(comment);
        } else {
            System.err.println("Comment " + comment + " не распознан");
        }
        return null;
    }
}
