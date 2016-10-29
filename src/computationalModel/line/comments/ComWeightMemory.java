package computationalModel.line.comments;

import computationalModel.line.CMLine;

/**
 * Created by BODY on 29.10.2016.
 */
public class ComWeightMemory extends Comment {
    @Override
    public void correct(CMLine cmLine) {
        cmLine.getProperties().setWeightMemory((byte)Integer.parseInt(lastComment));
    }
}
