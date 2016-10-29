package computationalModel.line.comments;

import computationalModel.line.CMLine;

/**
 * Created by BODY on 29.10.2016.
 */
public class ComWeightTime extends Comment {
    @Override
    public void correct(CMLine cmLine) {
        cmLine.getProperties().setWeightTime((byte)Integer.parseInt(lastComment));
    }
}
