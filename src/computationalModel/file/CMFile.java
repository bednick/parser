package computationalModel.file;

import computationalModel.line.CMLine;
import parser.LogCollector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Класс описывающий ВМ (либо её часть)
 */
public class CMFile {
    private ArrayList<CMLine> lines;
    private ArrayList<String> onlyInput;
    private LogCollector log;

    public CMFile(LogCollector log) {
        this.lines = new ArrayList<>();
        this.log = log;

    }

    public CMFile(ArrayList<CMLine> lines, LogCollector log) {
        this.lines = lines;
        this.log = log;
        //this.setOnlyInput();
    }

    public void readFile(String nameFile) throws IOException {
        /* Считываем файл, заполняем List из строк - CMLine
         * Считывание построчное
         * '\' - Символ переноса строки
         * Строки начинающиеся с символов '##' игнорируются
         * */
        String lastLine = Files.lines(Paths.get(nameFile), StandardCharsets.UTF_8)
                .filter(line->line.length() > 3)
                .filter(line->line.charAt(0) != '#' && line.charAt(1) != '#')
                .reduce((line_1,line_2)-> {
                    if (line_1.endsWith("\\")) {
                        return line_1.substring(0, line_1.length()-1)+line_2;
                    } else {
                        lines.add(new CMLine(line_1, log));
                        return line_2;
                    }
                }).orElse("");

        if (lastLine.isEmpty()) {
            log.addLine("File '" + nameFile + "' is EMPTY");
        } else {
            if (lastLine.endsWith("\\")) {
                lines.add(new CMLine(lastLine.substring(0, lastLine.length()-1), log));
            } else {
                lines.add(new CMLine(lastLine, log));
            }
        }
    }

    public ArrayList<CMLine> getForIn(String name) {
        /*  Получение ArrayList содержащий все строки содержащие в списке входных файлов файл с именем параметра
         *  если таких нет, возвращается ArrayList без элементов
         * */
        ArrayList<CMLine> newArrayList = new ArrayList<>();
        for (CMLine i : lines) {
            for (String str : i.getIn()) {
                if (str.equals(name)) {
                    newArrayList.add(i);
                    break;
                }
            }
        }

        return newArrayList;
    }

    public ArrayList<CMLine> getForOut(String name) {
        /* Получение ArrayList содержащий все строки содержащие в списке выходных файлов файл с именем параметра
         * если таких нет, возвращается ArrayList без элементов
         * */
        ArrayList<CMLine> newArrayList = new ArrayList<>();
        for (CMLine i : lines) {
            if ( name.equals(i.getProperties().getFilesMark()) ) {
                newArrayList.add(i);
                continue;
                // todo проверку в считывании файла (Проверка в слитывании марки, что его нет в выхолных файлах)
            }
            for (String str : i.getOut()) {
                if (str.equals(name)) {
                    newArrayList.add(i);
                    break;
                }
            }

        }
        return newArrayList;
    }

    public ArrayList<CMLine> getForCommand(String name) {
        /* Получение ArrayList содержащий все строки содержащие заданную команду
         * если таких нет, возвращается ArrayList без элементов
         * */
        ArrayList<CMLine> newArrayList = new ArrayList<>();
        for (CMLine i : lines) {
            if (i.getCommand().equals(name)) {
                newArrayList.add(i);
            }
        }
        return newArrayList;
    }

    public int getSize() {
        /* Возвращает количество строк в файле
        * */
        return lines.size();
    }

    public ArrayList<CMLine> getLines() {
        /* Возвращает ArrayList содержащий все строки ВМ
        * */
        return lines;
    }

    public ArrayList<String> getOnlyInput() {
        /*
        * Возращает список имён, которые нельзя получить из CM(но есть в какой-то строке во входящих файлах)
        * */
        if (onlyInput == null) {
            setOnlyInput();
        }
        return onlyInput;
    }

    private void setOnlyInput() {
        /* Выставляет список имён, которые нельзя получить из CM(но есть в какой-то строке во входящих файлах)
        * */
        ArrayList<String> newOnlyInput = new ArrayList<>();
        if (lines.size() == 0) {
            onlyInput = newOnlyInput;
            return;
        }
        ArrayList<String> allIn = new ArrayList<>();
        ArrayList<String> allOut = new ArrayList<>();
        for (CMLine cmLine : lines) {
            for (String nameIn : cmLine.getIn()) {
                allIn.add(nameIn);
            }
            for (String nameOut : cmLine.getOut()) {
                allOut.add(nameOut);
            }
        }
        for (String name : allIn) {
            if (!allOut.contains(name)) {
                if (!newOnlyInput.contains(name)) {
                    newOnlyInput.add(name);
                }
            }
        }
        onlyInput = newOnlyInput;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (CMLine line : lines) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
