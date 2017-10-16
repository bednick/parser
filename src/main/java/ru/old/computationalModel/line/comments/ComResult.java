package main.java.ru.old.computationalModel.line.comments;

import main.java.ru.old.computationalModel.line.CMLine;
import main.java.ru.old.parser.LogCollector;

/**
 * Выставляет возвращаемое значение, обозначающее корректное завершение работы
 */
public class ComResult extends Comment {

    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        cmLine.getProperties()
                .setCorrectReturnValue(Integer.parseInt(lastComment));
    }
}
