package computationalModel.line.comments;

import computationalModel.line.CMLine;
import parser.LogCollector;

/**
 * создаёт примечание, что при определённом возвращаемом значении создаётся файл
 * коментарий вида:
 * <ВОЗВРАЩАЕМОЕ_ЗНАЧЕНИЕ>DELIMITER_CREATEMARK<ИМЯ_ФАЙЛА_МЕТКИ>
 */
public class ComCreateMark extends Comment {
    public static String DELIMITER_CREATEMARK = "=";
    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        String buf[] = lastComment.split(DELIMITER_CREATEMARK,2);
        cmLine.getProperties().getFilesMarks().put(Integer.parseInt(buf[0]), buf[1]);
    }
}
