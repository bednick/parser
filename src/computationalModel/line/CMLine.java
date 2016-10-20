package computationalModel.line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс реализующий строку в вычислительной модели
 */
public class CMLine implements Comparable<CMLine>{
    public static final String DELIMITERCOMMENT = "#" ;
    public static final String DELIMITERROLE = ";" ;
    public static final String DELIMITER = " ";

    private String[] in;
    private String[] out;
    private String command;
    private String[] comments;

    private Properties properties;
    private Flags flags;

    public CMLine(String line){
        this.in = ((line.split(DELIMITERROLE,3)[0]).split(DELIMITER));
        this.out = ((line.split(DELIMITERROLE,3)[1]).split(DELIMITER));
        this.command = line.split(DELIMITERROLE, 3)[2].split(DELIMITERCOMMENT, 2)[0];
        if(line.split(DELIMITERROLE,3)[2].split(DELIMITERCOMMENT,2).length == 1) {
            this.comments = null;
        } else{
            this.comments = ((line.split(DELIMITERROLE, 3)[2].split(DELIMITERCOMMENT, 2)[1]).split(DELIMITER));
        }
        this.properties = new Properties();
        this.flags = new Flags();
    }
    public String[] getIn() {
        return in;
    }
    public String[] getOut() {
        return out;
    }
    public String getCommand() {
        return command;
    }
    public String[] getComments() {
        return comments;
    }
    public Flags getFlags() {
        return flags;
    }
    public Properties getProperties() {
        return properties;
    }
    @Override
    public int compareTo(CMLine o) {
        return  command.compareTo(o.command);
    }


    public class Flags {
        /**
         * Класс, хранящий в себе все флаги класса CMLine,
         * в частности которыми опперируют Comment'ы
         */
        private boolean rubbishOut; // считать ли мусором выходные файлы
        private boolean canPerform; // можно ли выполнять эту команду, или же один из входных файлов нельзя никак получить
        private boolean start;      // была ли запущенна эта строка на выполнени
        private boolean finish;     // было ли завершено выполнение


        public Flags(){
            this.rubbishOut = true;
            this.canPerform = true;
            this.start = false;
            this.finish = false;
        }
        public boolean isRubbishOut() {
            return rubbishOut;
        }
        public boolean isCanPerform() {
            return canPerform;
        }
        public boolean isStart() {
            return start;
        }
        public boolean isFinish() {
            return finish;
        }


        public void setRubbishOut(boolean rubbishOut) {
            this.rubbishOut = rubbishOut;
        }
        public void setCanPerform(boolean canPerform) {
            this.canPerform = canPerform;
        }
        public void setStart(boolean start) {
            this.start = start;
        }
        public void setFinish(boolean finish) {
            this.finish = finish;
        }
    }
    public class Properties {
        /**Класс, хранящий в себе все свойства класса CMLine,
         * в частности которыми опперируют Comment'ы
         * например вес, вес относительно
         */
        /*значение весов изменяется от 0 до 100
        * 0 - минимальный приоритет
        * 100 - максимальный приоритет
        * */
        public final byte MAXWEIGHT = 100;
        public final byte MINWEIGHT = 0;


        private byte weight;        //вес, используемый при работе(вычесляется на основе остальных весов)
        private byte weightTime;    //вес, отвечающий за скорость работы
        private byte weightMemory;  //вес, отвечающий за память, занимающую процессом
        private int correctReturnValue; // Какое возвращаемое значение считать корректным
        private Map<Integer, String> filesMarks; // хранит Integer - возвращаемое значение при выполнении, String - имя файла, который нужно создать
        private ArrayList<String> filesNotRubbish;

        public Properties(){
            weight = (MAXWEIGHT - MINWEIGHT) / 2;
            weightTime = (MAXWEIGHT - MINWEIGHT) / 2;
            weightMemory = (MAXWEIGHT - MINWEIGHT) / 2;
            correctReturnValue = 0;
            filesMarks = null;
            filesNotRubbish = null;
        }

        public byte getWeight() {
            return weight;
        }
        public byte getWeightMemory() {
            return weightMemory;
        }
        public byte getWeightTime() {
            return weightTime;
        }
        public int getCorrectReturnValue() {
            return correctReturnValue;
        }
        public Map<Integer, String> getFilesMarks(){
            if(filesMarks == null){
                filesMarks = new HashMap<>();
            }
            return filesMarks;
        }
        public ArrayList<String> getFileNotRubbish() {
            if(filesNotRubbish == null){
                filesNotRubbish = new ArrayList<>();
            }
            return filesNotRubbish;
        }
        public void setCorrectReturnValue(int correctReturnValue) {
            this.correctReturnValue = correctReturnValue;
        }
        public void setWeight(byte weight) {
            if(weight > MAXWEIGHT) {
                this.weight = MAXWEIGHT;
            } else if(weight < MINWEIGHT) {
                this.weight = MINWEIGHT;
            } else {
                this.weight = weight;
            }
        }
        public void setWeightMemory(byte weightMemory) {
            if(weightMemory > MAXWEIGHT) {
                this.weightMemory = MAXWEIGHT;
            } else if(weightMemory < MINWEIGHT) {
                this.weightMemory = MINWEIGHT;
            } else {
                this.weightMemory = weightMemory;
            }
        }
        public void setWeightTime(byte weightTime) {
            if(weightTime > MAXWEIGHT) {
                this.weightTime = MAXWEIGHT;
            } else if(weightTime < MINWEIGHT) {
                this.weightTime = MINWEIGHT;
            } else {
                this.weightTime = weightTime;
            }
        }
    }
}
