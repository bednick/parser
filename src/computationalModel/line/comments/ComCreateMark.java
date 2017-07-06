package computationalModel.line.comments;

import computationalModel.line.CMLine;
import parser.LogCollector;

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
