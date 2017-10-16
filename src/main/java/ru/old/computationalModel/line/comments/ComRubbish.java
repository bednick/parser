package main.java.ru.old.computationalModel.line.comments;

import main.java.ru.old.computationalModel.line.CMLine;
import main.java.ru.old.parser.LogCollector;

/**
 * Выставляет флаг означающий, что выходные файлы являются или не являются мусором
 */
public class ComRubbish extends Comment {

    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        if (lastComment.equals("true") || lastComment.equals("yes")) {
            cmLine.getFlags().setRubbishOut(true);
        } else if (lastComment.equals("false") || lastComment.equals("no")) {
            cmLine.getFlags().setRubbishOut(false);
        } else {
            System.err.println("не верные параметры у коментария rubbish: " + lastComment);
            log.addLine("не верные параметры у коментария rubbish: " + lastComment);
        }
    }
}
