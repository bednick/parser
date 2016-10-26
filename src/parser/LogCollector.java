package parser;

import java.io.*;
import java.util.ArrayList;

/**
 * Класс реализует работу с логами,
 * сбор производиться если выставлен флаг "working"
 * если в кострукторе не переданно имя, то пишется в <logParser.log>
 */
public class LogCollector {
    private ArrayList<String> log;
    private String nameLog;                     //имя файла лога
    private boolean working;                    //собирать ли лог

    public LogCollector() {
        this.nameLog = "logParser.log";
        this.working = true;
        this.log = new ArrayList<>();
    }
    public LogCollector(String nameLog, boolean working) {
        this.nameLog = nameLog;
        this.working = working;
        this.log = new ArrayList<>();
    }

    public boolean isWorking() {
        return working;
    }
    public void setWorking(boolean working) {
        this.working = working;
    }
    public String getNameLog() {
        return nameLog;
    }
    public void addLine(String string) {
        if(working){
            log.add(string);
            log.add("\n");
            try {
                push();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void push() throws IOException {
        /*
        * Произвести запись лога в файл, и очистить log
        * */
        if(working && (log.size() != 0)){
            File file = new File(nameLog);
            if(file.exists()){
                if(file.canWrite()){
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                    for (String line: log) {
                        writer.write(line);
                    }
                    writer.flush();
                    writer.close();
                    log.clear();
                } else {
                    throw new IOException("Ошибка записис в log file");
                }
            } else {
                if(!file.createNewFile()){
                    throw new IOException("Ошибка создания log file");
                }
                this.push();
            }
        }
    }
}
