package computationalModel.line.comments;

import computationalModel.line.CMLine;

/**
 * Выставляет возвращаемое значение, обозначающее корректное завершение работы
 */
public class ComResult extends Comment {

    @Override
    public void correct(CMLine cmLine) {
        cmLine.getProperties()
                .setCorrectReturnValue(Integer.parseInt(lastComment));
    }
}
