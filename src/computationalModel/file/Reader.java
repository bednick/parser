package computationalModel.file;

import computationalModel.line.CMLine;
import parser.LogCollector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by BODY on 09.07.2017.
 */
public class Reader {
    private CMFile cmFile;
    private LogCollector log;
    private Variables variables;

    Reader(CMFile cmFile, LogCollector log) {
        this.cmFile = cmFile;
        this.log = log;
        this.variables = new Variables(log);
    }

    void readFile(String nameFile) throws IOException {
        /* Считываем файл, заполняем List из строк - CMLine
         * Считывание построчное
         * '\' - Символ переноса строки
         * Строки начинающиеся с символов '##' игнорируются
         * Строки начинающиеся с '#' расцениваются как специальные команды
         * */
        String lastLine = Files.lines(Paths.get(nameFile), StandardCharsets.UTF_8)
                .filter(line-> (line.length() == 1)
                        || (line.length() >= 2 && line.charAt(0) != '#')
                        || (line.length() >= 2 && line.charAt(0) == '#' && line.charAt(1) != '#'))
                .reduce((line_1,line_2)-> {
                    if (line_1.endsWith("\\")) {
                        return line_1.substring(0, line_1.length()-1)+line_2;
                    } else {
                        processLine(line_1);
                        return line_2;
                    }
                }).orElse("");

        if (lastLine.isEmpty()) {
            log.addLine("File '" + nameFile + "' is EMPTY");
        } else {
            if (lastLine.endsWith("\\")) {
                processLine(lastLine.substring(0, lastLine.length()-1));
            } else {
                processLine(lastLine);
            }
        }
    }

    private void processLine(String line) {
        if (variables.isNeedSubstitute(line)){
            line = variables.substitute(line);
        }
        if (line.startsWith(variables.getStartWith())) {
            variables.put(line);
        } else {
            cmFile.add(new CMLine(line, log));
        }
    }

}
