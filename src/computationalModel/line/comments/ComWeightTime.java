package computationalModel.line.comments;

import computationalModel.line.CMLine;
import parser.LogCollector;

/**
 * Created by BODY on 29.10.2016.
 */
public class ComWeightTime extends Comment {
    @Override
    public void correct(CMLine cmLine, LogCollector log) {
        cmLine.getProperties().setWeightTime((byte) Integer.parseInt(lastComment));
        log.addLine(cmLine.toString() + " set weight time=" + (byte) Integer.parseInt(lastComment));
    }
}
