package main.java.ru.old.computationalModel.line.comments;

import main.java.ru.old.computationalModel.line.CMLine;
import main.java.ru.old.parser.LogCollector;

/**
 * абстрактный класс, реализующий коментарии вида:
 * <имя_комента>DELIMITER<строка_описывающее_действие>
 * <строка_описывающее_действие> - храниться в lastComment(парамметр для любого комментария)
 */
public abstract class Comment {
    public static String DELIMITER = "=";
    protected String lastComment;

    public Comment() {
        lastComment = null;
    }

    protected abstract void correct(CMLine cmLine, LogCollector log);

    public void correct(CMLine cmLine, String comment, LogCollector log) {
        lastComment = comment;
        correct(cmLine, log);
    }
}
