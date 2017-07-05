package parser;

import computationalModel.line.CMLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс реализующий уничтожение временных файлов
 */
public class RubbishCollector {
    private LogCollector log;
    private List<String> rubbish;
    private List<String> startEnvironment;
    private List<String> outFileParser;

    RubbishCollector(LogCollector logCollector) {
        this.rubbish = new ArrayList<>();
        this.startEnvironment = new ArrayList<>();
        this.log = logCollector;
    }

    public void setStartEnvironment(String dirName) {
        if (dirName == null) {
            return;
        }
        setStartEnvironment(new File(dirName));
    }

    private void setStartEnvironment(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir + " is not directory");
        }
        File[] files = dir.listFiles();
        startEnvironment.clear();
        if (files != null) {
            Arrays.stream(files).forEach(file -> startEnvironment.add(file.getName()));
        }
    }

    public void setOutFileParser(List<String> outFileParser) {
        this.outFileParser = outFileParser;
    }

    public void addRubbish(CMLine cmLine) {
        /*
        * Добавить в мусор выходные файлы, при условии что они помечены как мусор
        * */
        if (cmLine.getFlags().isRubbishOut()) {
            for (String str : cmLine.getOut()) {
                if (cmLine.getProperties().getFileNotRubbish() != null) {
                    if (cmLine.getProperties().getFileNotRubbish().contains(str)) {
                        continue;
                    }
                }
                rubbish.add(str);
            }
        }
    }

    public void addRubbish(String nameFile, CMLine cmLine) {
        if (cmLine.getFlags().isRubbishOut()) {
            if (!cmLine.getProperties().getFileNotRubbish().contains(nameFile)) {
                rubbish.add(nameFile);
            }
        }
    }

    public void clear() {
        /*
        * Удаление файлов, за исключение входных файлов парсера ( outParser )
        * и стартового окружения
        * */
        log.addLine("start delete files");
        for (String str : rubbish) {
            if (!outFileParser.contains(str) && !startEnvironment.contains(str)) {
                File file = new File(str);
                if (file.exists()) {
                    if (file.delete()) {
                        log.addLine("delete file " + file.getName());
                    } else {
                        log.addLine("error delete file " + file.getName());
                    }
                }
            }
        }
        log.addLine("finish delete files");
        rubbish.clear();
    }

    public void deleteAfterError(CMLine cmLine) {
        log.addLine("start delete files after error");
        for (String str : cmLine.getOut()) {
            if (!startEnvironment.contains(str) && !cmLine.getProperties().getFileNotRubbish().contains(str)) {
                File file = new File(str);
                if (file.exists()) {
                    if (file.delete()) {
                        log.addLine("delete file " + file.getName());
                    } else {
                        log.addLine("error delete file " + file.getName());
                    }
                }
            }
        }
        log.addLine("finish delete files after error");
    }
}
