package parser;

import computationalModel.line.CMLine;

import java.io.File;
import java.util.ArrayList;

/**
 * Класс реализующий уничтожение временных файлов
 */
public class RubbishCollector {
    private LogCollector log;
    private ArrayList<String> rubbish;
    private boolean working;

    public RubbishCollector(LogCollector logCollector) {
        this.rubbish = new ArrayList<>();
        this.log = logCollector;
        this.working = true;
    }

    public RubbishCollector(LogCollector logCollector, boolean working) {
        this.rubbish = new ArrayList<>();
        this.log = logCollector;
        this.working = working;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
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

    public void clear(ArrayList<String> outParser) {
        /*
        * Удаление файлов, за исключение входных файлов парсера ( outParser )
        * */
        log.addLine("start delete files");
        for (String str : rubbish) {
            if (!outParser.contains(str)) {
                File file = new File(str);
                if (file.exists()) {
                    if (file.delete()) {
                        log.addLine("detele file " + file.getName());
                    } else {
                        log.addLine("error delete file " + file.getName());
                    }
                }
            }
        }
        log.addLine("finish delete files");
        rubbish.clear();
    }

}
