package main.java.ru.old.computationalModel.line.comments;

import main.java.ru.old.computationalModel.line.CMLine;
import main.java.ru.old.parser.LogCollector;

/**
 * помечает только один выходной файл как не мусор
 */
public class ComNotRubbish extends Comment {

    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        for (String str : cmLine.getOut()) {
            if (str.equals(lastComment)) {
                cmLine.getProperties().getFileNotRubbish().add(lastComment);
                break;
            }
        }
    }
}
