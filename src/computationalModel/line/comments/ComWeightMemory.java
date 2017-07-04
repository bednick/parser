package computationalModel.line.comments;

import computationalModel.line.CMLine;
import parser.LogCollector;

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
