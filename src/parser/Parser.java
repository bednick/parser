package parser;

import computationalModel.file.CMFile;
import computationalModel.line.CMLine;
import computationalModel.tree.CMTree;
import computationalModel.tree.CMTreeVertex;
import parser.performing.Perform;
import parser.performing.PerformUnix;
import parser.performing.PerformWin;
import parser.view.View;
import parser.view.ViewConsole;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Основной класс
 * <p>
 * <p>
 * Проблема
 * Нужно ограничить количество fileMark'ов до 1 (метка только для корректного выполнения)
 * Или как правильно реагировать на несколько файлов меток?! (именно про выполнение)
 */
public class Parser {
    private Parameters parameters;
    private final Lighthouse lighthouse;
    private CMFile cmFile;
    private LogCollector logCollector;
    private RubbishCollector rubbishCollector;
    private ConfigFile configFile;
    private Perform perform;
    private String path_environment;// путь к окружению
    private String path_parser;     // путь к *.jar

    private View view;


    public Parser(View view) {
        this.view = view;
        this.lighthouse = new Lighthouse();
        this.logCollector = new LogCollector();
        this.cmFile = new CMFile(logCollector);
        this.rubbishCollector = new RubbishCollector(logCollector);
        String os = System.getProperty("os.name" );
        if (os.contains("Win")) {
            perform = new PerformWin();
        } else {
            perform = new PerformUnix();
        }
        try {
            path_environment = new File(".").getCanonicalPath();
        } catch (IOException e) {
            logCollector.addLine(e.toString());
            path_environment = new File(".").getAbsolutePath();
        }
        path_parser = new File(Parser.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();

        logCollector.addLine("\n\nSTART SYSTEM");
        logCollector.addLine("OS = " + System.getProperty("os.name") );
        logCollector.addLine("PATH_environment = "+path_environment);
        logCollector.addLine("PATH_parser      = "+path_parser);
        configFile = new ConfigFile(path_environment, path_parser, logCollector);
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    private boolean start() throws IOException {
        /*
        * Основная логика parser'а
        * */
        try {
            logCollector.addLine("\n\nSTART PARSER " + new java.util.Date().toString());
            logCollector.addLine("");
            logCollector.addLine("SET ENVIRONMENT");
            rubbishCollector.setStartEnvironment(path_environment);
            if (parameters.namesFileOut.size() == 0) {
                logCollector.addLine("SIZE FILES OUT == 0");
                return true;
            }
            if (readCMFiles() == 0) {
                logCollector.addLine("FILES CM NOT FOUND");
                view.println("FILES CM NOT FOUND");
                return false;
            }
            if (parameters.isMemory()) {
                logCollector.addLine("OPTIMIZATION parameter : memory");
            } else {
                logCollector.addLine("OPTIMIZATION parameter : time");
            }
            for (String nameOut : parameters.getNamesFileOut()) {
                CMTree tree = new CMTree(cmFile, nameOut);

                do {
                    logCollector.push();
                    boolean isCanPerform = false;
                    for (CMTreeVertex ver : setMinWeight(tree).getHead()) { // Выставляем веса!
                        if (ver.getCmLine().getFlags().isCanPerform()) {
                            isCanPerform = true;
                            break;
                        }
                    }
                    logCollector.addLine("TREE:");
                    logCollector.addLine(tree.toString());
                    if (!isCanPerform) {
                        view.println("FILE '" + nameOut + "' CAN NOT GET!");
                        return false;
                    }
                    logCollector.push();
                } while (!performMinBranch(tree));
            }
            return true;
        } catch (IOException e) {
            logCollector.addLine(e.toString());
            throw e;
        } finally {
            rubbishCollector.setOutFileParser(parameters.getNamesFileOut());
            rubbishCollector.clear();
            logCollector.addLine("\nFINISH PARSER " + new java.util.Date().toString());
            logCollector.push();
        }
    }

    private int readCMFiles() {
        int countCMFile = 0;
        for (String str : parameters.getNamesFileCM()) {//считываем все входные CM
            if (readCMFile(str)) {
                ++countCMFile; // Если не удалось найти такой вычислительной модели по всем путям
            }
        }
        return countCMFile;
    }

    private boolean readCMFile(String nameFile) {
        List<String> allPath = new ArrayList<>();
        for(String path: configFile.getPathCM()) {
            try {
                cmFile.readFile(path+System.getProperty("file.separator")+nameFile);
                logCollector.addLine("NAME CM '" + path+System.getProperty("file.separator")+nameFile+ "'");
                return true;

            } catch (IOException e) {
                logCollector.addLine("Error path: " +path+System.getProperty("file.separator")+nameFile);
            }
        }
        return false;
    }

    private CMTree setMinWeight(CMTree tree) {
        /*
        * выставляет у каждой вершины её вес(зависит от параметров Parser'a)
        * если вершина является недоступной, помечает это
        * */
        Queue<CMTreeVertex> queue = new LinkedList<>(); // Для правильного добавления в стек
        Stack<CMTreeVertex> stack = new Stack<>();      // Последовательность всех вершин, так чтобы при доставании элемента
        // у всех входящих вершин были выставлены веса
        boolean isTime = (parameters.isTime() && !parameters.isMemory())  //определяем, по какому критерию ищем min вес
                || (!parameters.isTime() && !parameters.isMemory());
        for (CMTreeVertex head : tree.getHead()) {
            queue.add(head);
            stack.add(head);
        }
        CMTreeVertex vertex;
        while ((vertex = queue.poll()) != null) { //заполняем стек
            for (String nameIn : vertex.getCmLine().getIn()) {
                for (CMTreeVertex inVertex : vertex.getIn(nameIn)) {
                    if (stack.contains(inVertex)) {
                        stack.remove(inVertex);
                    }
                    queue.add(inVertex);
                    stack.add(inVertex);
                }
            }
        }
        while (!stack.empty()) {
            vertex = stack.pop();      //берём эмелент и находим минимальный вес
            if (vertex.getCmLine().getFlags().isCanPerform()) { // при условии, что эту строчку можно выполнить
                int minWeightVertex = 0;
                vertex.getCmLine().getProperties().setWeight(0);
                logCollector.addLine("VERTEX : " + vertex.getCmLine().getCommand());
                for (String nameIn : vertex.getCmLine().getIn()) {
                    int minWeightFile = Integer.MAX_VALUE;      //минимальный вес, во всех входящих строчках, для ОТДЕЛЬНОГО ВХОДНОГО ФАЙЛА
                    CMTreeVertex minInCMTreeVertex = null;
                    if ((new File(nameIn).exists())) {   // если такой файл существует
                        minWeightFile = 0;
                    } else {
                        for (CMTreeVertex inVertex : vertex.getIn(nameIn)) {
                            if (inVertex.getCmLine().getFlags().isCanPerform()) {
                                if (inVertex.getCmLine().getProperties().getWeight() < minWeightFile) {
                                    minWeightFile = inVertex.getCmLine().getProperties().getWeight();
                                    minInCMTreeVertex = inVertex;
                                }
                            }
                        }
                    }
                    if (minWeightFile == Integer.MAX_VALUE) {
                        vertex.getCmLine().getFlags().setCanPerform(false);
                        vertex.getCmLine().getProperties().setWeight(vertex.getCmLine().getProperties().INFINITE_WEIGHT);
                        logCollector.addLine("file '" + nameIn + "'can not perform");
                        break;
                    }
                    logCollector.addLine("file '" + nameIn + "' minWeight =" + minWeightFile);
                    minWeightVertex += minWeightFile;
                    vertex.setMinInVertex(nameIn, minInCMTreeVertex);
                }
                if (vertex.getCmLine().getProperties().INFINITE_WEIGHT != vertex.getCmLine().getProperties().getWeight()) {
                    if (isTime) {
                        minWeightVertex += vertex.getCmLine().getProperties().getWeightTime();
                    } else {
                        minWeightVertex += vertex.getCmLine().getProperties().getWeightMemory();
                    }
                    logCollector.addLine("MIN weight vertex == " + minWeightVertex);
                    vertex.getCmLine().getProperties().setWeight(minWeightVertex);
                } else {
                    logCollector.addLine("vertex '" + vertex.getCmLine().getCommand() + "' can not perform");
                }
            }
        } // у всех вершин выставлен вес
        return tree;
    }

    private boolean performMinBranch(CMTree tree) throws IOException {
        CMTreeVertex minVertexHead = null;
        int minWeight = Integer.MAX_VALUE;
        logCollector.addLine("HEADS AND WEIGHTS");
        for (CMTreeVertex head : tree.getHead()) {
            if (head.getCmLine().getFlags().isCanPerform()) {
                logCollector.addLine("weight ' " + head + " ' == "
                        + head.getCmLine().getProperties().getWeight());
                if (head.getCmLine().getProperties().getWeight() < minWeight) {
                    minVertexHead = head;
                    minWeight = head.getCmLine().getProperties().getWeight();
                }
            }
        }
        ArrayList<CMLine> branch = new ArrayList<>();
        Queue<CMTreeVertex> queue = new LinkedList<>();
        queue.add(minVertexHead);
        CMTreeVertex vertex;
        while ((vertex = queue.poll()) != null) {
            if (!branch.contains(vertex.getCmLine())) {
                branch.add(vertex.getCmLine());
            }
            for (String nameIn : vertex.getCmLine().getIn()) {
                if (vertex.getMinIn(nameIn) != null) {
                    queue.add(vertex.getMinIn(nameIn));
                }
            }
        }

        CMFile cmFile = new CMFile(branch, logCollector);
        logCollector.addLine("BRANCH:");
        logCollector.addLine(cmFile.toString());
        return performCMFile(cmFile);
    }

    private boolean performCMLine(CMLine cmLine) {
        try {
            //Исполнение команды(шага) и проерка его результата
            cmLine.getFlags().setStart(true);
            Process pr = perform.start(cmLine, logCollector);
            boolean rez = (pr.exitValue() == cmLine.getProperties().getCorrectReturnValue());
            if (!rez) {
                cmLine.getFlags().setCanPerform(false);
                logCollector.addLine("incorrect return value " + cmLine.getCommand());
                rubbishCollector.deleteAfterError(cmLine);
            } else {
                logCollector.addLine("correct return value " + cmLine.getCommand());
                // создание файла метки, если это необходимо
                if (!cmLine.getProperties().isNullFilesMarks()) {
                    File fileMark = new File(cmLine.getProperties().getFilesMark());
                    if (!fileMark.exists()) {
                        if (fileMark.createNewFile()) {
                            logCollector.addLine("CREATE file mark: " + fileMark.getName());
                            rubbishCollector.addRubbish(fileMark.getName(), cmLine);
                        } else {
                            logCollector.addLine("ERROR CREATE file mark: " + fileMark.getName());
                        }
                    } else {
                        logCollector.addLine("EXISTS file mark: '" + fileMark.getName() + "' (not rubbish)");
                    }

                }
                rubbishCollector.addRubbish(cmLine);
            }
            return rez;
        } catch (IOException e) {
            logCollector.addLine(e.toString());
            cmLine.getFlags().setCanPerform(false);
            return false;
        } finally {
            cmLine.getFlags().setFinish(true);
            logCollector.addLine("finish " + cmLine.getCommand());
        }
    }

    private boolean performCMFile(CMFile branch) throws IOException {
        boolean flag = true;
        lighthouse.setError(false);
        int count = 0; //количесто выполненых операций
        while (flag) {

            for (CMLine line : branch.getLines()) {
                if (line.getFlags().isStart()) { // если строка уже была запушенна ранее, то и проверять её не нужно более
                    continue;
                }
                boolean canPerform = true;
                //Проверяем, можем ли выполнить команду
                for (String nameIn : line.getIn()) {
                    if (!(new File(nameIn).exists())) {
                        canPerform = false;
                        break;
                    }
                }
                if (canPerform) {
                    lighthouse.increment();
                    line.getFlags().setStart(true);
                    CompletableFuture<Void> start = CompletableFuture.completedFuture(performCMLine(line))
                            .thenAcceptAsync((x) -> {
                                if (!x) {
                                    lighthouse.setError(true);
                                }
                                synchronized (lighthouse) {
                                    --lighthouse.count;
                                    lighthouse.notifyAll();
                                }
                            });
                    ++count;
                }
                if (count == branch.getSize()) {// TODO: 24.10.2016 Реализовать правильную проверку на завершения вычисления ветви
                    flag = false;
                }
            }
            if (count == 0) {
                //System.err.println("error branch");
                logCollector.addLine("error branch");
                return false;
            }
            synchronized (lighthouse) {
                if (lighthouse.count != 0) {
                    try {
                        lighthouse.wait();
                    } catch (InterruptedException e) {
                        System.err.println(e.toString());
                    }
                }
                if (lighthouse.error) {

                    while (lighthouse.count > 0) {
                        try {
                            lighthouse.wait();
                        } catch (InterruptedException e) {
                            System.err.println(e.toString());
                        }
                    }
                    return false;
                }
            }
        }

        return true;
    }

    public static void main(String[] args) {
        View view = new ViewConsole();
        try {
            Parser parser = new Parser(view);
            view.setParser(parser);
            view.start(args);
            parser.setParameters(view.getParameters());
            if (parser.start()) {
                view.println("Successfully");
                System.exit(0);
            } else {
                view.println("Unsuccessfully");
                System.exit(1);
            }
        } catch (IOException e) {
            view.printError(e.getMessage());
            System.exit(2);
        }
    }

    public static class Parameters {
        /* Хранит в себе все параметры,
        *
        * */
        private boolean time = false;
        private boolean memory = false;
        private ArrayList<String> namesFileCM;
        private ArrayList<String> namesFileOut;

        public Parameters() {
            namesFileCM = new ArrayList<>();
            namesFileOut = new ArrayList<>();
        }

        public void setTime(boolean time) {
            this.time = time;
        }

        public void setMemory(boolean memory) {
            this.memory = memory;
        }

        public void addFileCM(String nameFile) {
            namesFileCM.add(nameFile);
        }

        public void addFileOut(String nameFile) {
            namesFileOut.add(nameFile);
        }

        public boolean isTime() {
            return time;
        }

        public boolean isMemory() {
            return memory;
        }

        public ArrayList<String> getNamesFileCM() {
            return namesFileCM;
        }

        public ArrayList<String> getNamesFileOut() {
            return namesFileOut;
        }
    }

    public class Lighthouse {
        /*
        * Вспомогательный класс, используется потоками для общения между собой
        * Хранит в себе количество выполняемых задач, и метку
        * указывающую на то, что во время выполнения одной из задач произошла ошибка
        * */
        private int count;
        private volatile boolean error;

        Lighthouse() {
            count = 0;
        }

        synchronized void increment() {
            count++;
        }

        synchronized void decrement() {
            count--;
        }

        synchronized void setError(boolean error) {
            this.error = error;
        }
    }
}
