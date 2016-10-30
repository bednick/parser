package computationalModel.line.comments;

import computationalModel.line.CMLine;
import parser.LogCollector;

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
