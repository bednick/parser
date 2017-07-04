package computationalModel.file;

import computationalModel.line.CMLine;
import parser.LogCollector;

import java.io.*;
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
        /* Считываем файл, заполняем List из строк
         * Считывание построчное
         * */
        BufferedReader reader = null;
        try {
            File file = new File(nameFile);
            if (file.isFile() && file.canRead()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(nameFile)));
            } else {
                throw new IOException("Не удалось прочитать файл " + nameFile);
            }
            String buf;
            while ((buf = reader.readLine()) != null) {
                if (buf.length() > 2) {
                    if (buf.charAt(0) != '#' && buf.charAt(1) != '#') {
                        lines.add(new CMLine(buf, log));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Ошибка при открытии файла " + nameFile);
        } catch (IOException e) {
            throw new IOException("ошибка чтения файла " + nameFile);
        } finally {
            if (reader != null) {
                reader.close();
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
