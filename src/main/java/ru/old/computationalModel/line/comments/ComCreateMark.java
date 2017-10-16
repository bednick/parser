package main.java.ru.old.computationalModel.line.comments;

import main.java.ru.old.computationalModel.line.CMLine;
import main.java.ru.old.parser.LogCollector;

/**
 * создаёт примечание, что при определённом возвращаемом значении создаётся файл
 * коментарий вида:
 * <ВОЗВРАЩАЕМОЕ_ЗНАЧЕНИЕ>DELIMITER_CREATEMARK<ИМЯ_ФАЙЛА_МЕТКИ>
 */
public class ComCreateMark extends Comment {
    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        cmLine.getProperties().setFilesMark(lastComment);
    }
}
