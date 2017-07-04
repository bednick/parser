package computationalModel.tree;

import computationalModel.line.CMLine;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by BODY on 22.10.2016.
 */
public class CMTreeVertex {
    private HashMap<String, ArrayList<CMTreeVertex>> inVertex;
    private HashMap<String, CMTreeVertex> minInVertex;
    private CMLine cmLine;

    public CMTreeVertex(CMLine cmLine) {
        this.inVertex = new HashMap<>();
        this.minInVertex = new HashMap<String, CMTreeVertex>();
        this.cmLine = cmLine;
        for (String nameIn : cmLine.getIn()) {
            inVertex.put(nameIn, new ArrayList<>());
        }
    }

    public ArrayList<CMTreeVertex> getIn(String name) {
        return inVertex.get(name);
    }

    public void setMinInVertex(String nameIn, CMTreeVertex cmTreeVertex) {
        if (inVertex.containsKey(nameIn)) {
            minInVertex.put(nameIn, cmTreeVertex);
        } else {
            System.err.println(nameIn + " in not included file");
        }
    }

    public CMTreeVertex getMinIn(String name) {
        return minInVertex.get(name);
    }

    public CMLine getCmLine() {
        return cmLine;
    }

    @Override
    public String toString() {
        return cmLine.toString();
    }
}
