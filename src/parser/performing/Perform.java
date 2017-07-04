package parser.performing;

import computationalModel.line.CMLine;
import parser.LogCollector;

/**
 * Created by BODY on 04.07.2017.
 */
public abstract class Perform {

    public Process start(CMLine line, LogCollector logCollector) {
        return start(line);
    }

    abstract Process start(CMLine line);
}
