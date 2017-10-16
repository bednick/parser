package main.java.ru.old.computationalModel.line.comments;

import main.java.ru.old.computationalModel.line.CMLine;
import main.java.ru.old.parser.LogCollector;

/**
 * Created by BODY on 29.10.2016.
 */
public class ComWeightMemory extends Comment {
    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        cmLine.getProperties().setWeightMemory((byte) Integer.parseInt(lastComment));
        log.addLine(cmLine.toString() + " set weight memory=" + (byte) Integer.parseInt(lastComment));
    }
}
