package computationalModel.file;

import parser.LogCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BODY on 06.07.2017.
 */
public class Variables {
    private static final String START_WITH = "#define ";
    private static final String MARK = "$";
    private Map<String, String> variables;
    private LogCollector log;

    Variables(LogCollector log) {
        this.log = log;
        this.variables = new HashMap<>();
    }

    void put(String line) {
        if (line.startsWith(START_WITH)) {
            String[] var = line.split(" ", 3);
            if (var.length == 3) {
                variables.put(var[1], substitute(var[2]));
            } else {
                log.addLine("ERROR SYNTAX DEFINE: " + line);
            }
        } else {
            log.addLine("ERROR LINE: " + line);
        }
    }

    String substitute(String line) {
        for(Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            if (line.contains(MARK+key)) {
                return substitute(line.replace(MARK+key, entry.getValue()));
            }
        }
        return line;
    }

    String getStartWith() {
        return START_WITH;
    }

    boolean isNeedSubstitute(String line) {
        return line.contains(MARK);
    }
}
