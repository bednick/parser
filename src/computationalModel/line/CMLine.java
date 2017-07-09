package computationalModel.line;

import computationalModel.file.Variables;
import computationalModel.line.comments.Comment;
import computationalModel.line.comments.DownloadComment;
import parser.LogCollector;

import java.util.*;

/**
 * Класс реализующий строку в вычислительной модели
 */
public class CMLine implements Comparable<CMLine> {
    private static final String DELIMITER_COMMENT = "#";
    private static final String DELIMITER_ROLE = ";";
    private static final String DELIMITER = " ";

    private String[] in;
    private String[] out;
    private String command;

    private Properties properties;
    private Flags flags;

    public CMLine(String line, LogCollector log) {
        //todo пересивать всё на стримы
        try {
            this.in = Arrays.stream(((line.split(DELIMITER_ROLE, 3)[0]).split(DELIMITER))).filter(l -> !l.isEmpty()).toArray(String[]::new);
            this.out = Arrays.stream((line.split(DELIMITER_ROLE, 3)[1]).split(DELIMITER)).filter(l-> !l.isEmpty()).toArray(String[]::new);
            this.command = line.split(DELIMITER_ROLE, 3)[2].split(DELIMITER_COMMENT, 2)[0];

            String[] comments;
            if (line.split(DELIMITER_ROLE, 3)[2].split(DELIMITER_COMMENT, 2).length == 1) {
                comments = new String[0];
            } else {
                comments = ((line.split(DELIMITER_ROLE, 3)[2].split(DELIMITER_COMMENT, 2)[1]).split(DELIMITER));
            }
            this.properties = new Properties();
            this.flags = new Flags();
            for (String com : comments) {
                Comment comment = DownloadComment.getComment(com.split(Comment.DELIMITER, 2)[0]);
                if (comment != null) {
                    comment.correct(this, com.split(Comment.DELIMITER, 2)[1], log);
                } else {
                    System.err.println("invalid format comment :" + com);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(line);
            throw e;
        }
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

    public Flags getFlags() {
        return flags;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public int compareTo(CMLine o) {
        if (this == o) {
            return 0;
        }
        if (command.compareTo(o.command) == 0) {
            if (in.length != o.in.length) {
                return in.length - o.in.length;
            } else if (out.length != o.out.length) {
                return out.length - o.out.length;
            } else {
                return 0;
            }
        } else {
            return command.compareTo(o.command);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : in) {
            stringBuilder.append(str);
            stringBuilder.append(" ");
        }
        stringBuilder.append(";");
        for (String str : out) {
            stringBuilder.append(str);
            stringBuilder.append(" ");
        }
        stringBuilder.append(";");
        stringBuilder.append(command);
        return stringBuilder.toString();
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


        Flags() {
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
        /**
         * Класс, хранящий в себе все свойства класса CMLine,
         * в частности которыми опперируют Comment'ы
         * например вес, вес относительно
         */
        /*значение весов изменяется от 0 до 100
        * 0 - минимальный приоритет
        * 100 - максимальный приоритет
        * */
        public final byte MAX_WEIGHT = 100;
        public final byte MIN_WEIGHT = 0;
        //public final int DEFAULTCOLOR = -1;
        public final int INFINITE_WEIGHT = Integer.MAX_VALUE;

        private int weight;        //вес, используемый при работе( при вычислении оптимального пути)
        //private int color;         //используется при выборе оптимального пути () или нет
        private byte weightTime;    //вес, отвечающий за скорость работы
        private byte weightMemory;  //вес, отвечающий за память, занимающую процессом
        private int correctReturnValue; // Какое возвращаемое значение считать корректным
        private String filesMark; // хранит String - имя файла, который нужно создать при успешном выполнении
        private ArrayList<String> filesNotRubbish;

        public Properties() {
            this.weight = (MAX_WEIGHT - MIN_WEIGHT) / 2;
            //this.color = DEFAULTCOLOR;
            this.weightTime = (MAX_WEIGHT - MIN_WEIGHT) / 4;
            this.weightMemory = (MAX_WEIGHT - MIN_WEIGHT) / 4;
            this.correctReturnValue = 0;
            this.filesMark = null;
            this.filesNotRubbish = null;
        }

        public int getWeight() {
            return weight;
        }

        //public int getColor() {
        //    return color;
        //}
        public byte getWeightMemory() {
            return weightMemory;
        }

        public byte getWeightTime() {
            return weightTime;
        }

        public int getCorrectReturnValue() {
            return correctReturnValue;
        }

        public void setFilesMark(String filesMark) {
            this.filesMark = filesMark;
        }

        public String getFilesMark() {
            return filesMark;
        }

        public boolean isNullFilesMarks() {
            return filesMark == null;
        }

        public ArrayList<String> getFileNotRubbish() {
            if (filesNotRubbish == null) {
                filesNotRubbish = new ArrayList<>();
            }
            return filesNotRubbish;
        }

        public void setCorrectReturnValue(int correctReturnValue) {
            this.correctReturnValue = correctReturnValue;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        //public void setColor(int color) {
        //    this.color = color;
        //}
        public void setWeightMemory(byte weightMemory) {
            if (weightMemory > MAX_WEIGHT) {
                this.weightMemory = MAX_WEIGHT;
            } else if (weightMemory < MIN_WEIGHT) {
                this.weightMemory = MIN_WEIGHT;
            } else {
                this.weightMemory = weightMemory;
            }
        }

        public void setWeightTime(byte weightTime) {
            if (weightTime > MAX_WEIGHT) {
                this.weightTime = MAX_WEIGHT;
            } else if (weightTime < MIN_WEIGHT) {
                this.weightTime = MIN_WEIGHT;
            } else {
                this.weightTime = weightTime;
            }
        }
    }
}
