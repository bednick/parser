package computationalModel.line.comments;

import java.util.HashMap;
import java.util.Map;

/**
 *Класс хранящий в себе все доступные комментарии
 */
public class DownloadComment {
    private static Map<String, Comment> map;
    {
        map = new HashMap<>();
        map.put("result", new ComResult());
        map.put("rubbish", new ComRubbish());
        map.put("notRubbish", new ComNotRubbish());
        map.put("createMark", new ComCreateMark());
        map.put("time", new ComWeightTime());
        map.put("memory", new ComWeightMemory());
    }
    public static Comment getComment(String comment) {
        String buf[] = comment.split(Comment.DELIMITER, 2);//распарсить комент
        if(map.containsKey(buf[0])){
            map.get(buf[0]).setLastComment(buf[1]);
            return map.get(buf[0]);
        } else {
            System.err.println("Comment "+ buf[0] +" не распознан");
        }
        return null;
    }
}
