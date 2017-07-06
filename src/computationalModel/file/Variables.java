package computationalModel.file;

import parser.LogCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BODY on 06.07.2017.
 */
public class Variables {
    private static final String MARK = "$";
    private Map<String, String> variables;
    private LogCollector log;

    Variables(LogCollector log) {
        this.log = log;
        this.variables = new HashMap<>();
    }

    void put(String line) {
        if (line.startsWith("#define")) {
            String[] var = line.split(" ", 3);
            variables.put(var[1], var[2]);
        } else {
            log.addLine("ERROR LINE: " + line);
        }
    }

    public String substitute(String command) {
        for(Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            if (command.contains(MARK+key)) {
                command = command.replace(MARK+key, entry.getValue());
            }
        }
        return command;
    }

    public boolean isVariable(String command) {
        return command.contains(MARK);
    }
}
