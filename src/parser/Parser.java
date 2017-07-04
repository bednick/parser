package parser;

import computationalModel.file.CMFile;
import computationalModel.line.CMLine;
import computationalModel.tree.CMTree;
import computationalModel.tree.CMTreeVertex;

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


    public Parser() {
        this.parameters = new Parameters();
        this.lighthouse = new Lighthouse();
        this.logCollector = new LogCollector();
        this.cmFile = new CMFile(logCollector);
        this.rubbishCollector = new RubbishCollector(logCollector);
        this.configFile = new ConfigFile("");
    }

    private boolean start() throws IOException {
        /*
        * Основная логика parser'а
        * */
        try {
            logCollector.addLine("\nSTART PARSER " + new java.util.Date().toString());
            if (parameters.namesFileOut.size() == 0) {
                return false;
            }
            for (String str : parameters.getNamesFileCM()) {//считываем все входные CM
                cmFile.readFile(str);
                logCollector.addLine("NAME CM '" + str + "'");
            }
            if (parameters.isTime()) {
                logCollector.addLine("Parameter -t");
            } else if (parameters.isMemory()) {
                logCollector.addLine("Parameter -m");
            } else {
                logCollector.addLine("Parameter -t");
            }
            configFile.updateCMFile(cmFile, logCollector);
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
                    logCollector.addLine("set tree");
                    logCollector.addLine(tree.toString());
                    if (!isCanPerform) {
                        System.out.println("file '" + nameOut + "' can not get!");
                        return false;
                    }
                    logCollector.push();
                } while (!performMinBranch(tree));
            }
            return true;
        } finally {
            rubbishCollector.clear(parameters.getNamesFileOut());
            logCollector.addLine("\nFINISH PARSER " + new java.util.Date().toString());
            logCollector.push();
        }
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
        if (isTime) {
            logCollector.addLine("isTime optimization");
        }
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
                logCollector.addLine("vertex : " + vertex.getCmLine().getCommand());
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
                                /*if(isTime){
                                    if(inVertex.getCmLine().getProperties().getWeightTime() < minWeightFile){
                                        minWeightFile = inVertex.getCmLine().getProperties().getWeightTime();
                                        minInCMTreeVertex = inVertex;
                                    }
                                } else {
                                    if(inVertex.getCmLine().getProperties().getWeightMemory() < minWeightFile){
                                        minWeightFile = inVertex.getCmLine().getProperties().getWeightMemory();
                                        minInCMTreeVertex = inVertex;
                                    }
                                }*/
                            }
                        }
                    }
                    if (minWeightFile == Integer.MAX_VALUE) {
                        vertex.getCmLine().getFlags().setCanPerform(false);
                        vertex.getCmLine().getProperties().setWeight(vertex.getCmLine().getProperties().INFINITEWEIGHT);
                        logCollector.addLine("file '" + nameIn + "'can not perform");
                        break;
                    }
                    logCollector.addLine("file '" + nameIn + "' minWeight =" + minWeightFile);
                    minWeightVertex += minWeightFile;
                    vertex.setMinInVertex(nameIn, minInCMTreeVertex);
                }
                if (vertex.getCmLine().getProperties().INFINITEWEIGHT != vertex.getCmLine().getProperties().getWeight()) {
                    if (isTime) {
                        minWeightVertex += vertex.getCmLine().getProperties().getWeightTime();
                    } else {
                        minWeightVertex += vertex.getCmLine().getProperties().getWeightMemory();
                    }
                    logCollector.addLine("minWeightVertex == " + minWeightVertex);
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
        logCollector.addLine("performMinBranch");
        for (CMTreeVertex head : tree.getHead()) {
            if (head.getCmLine().getFlags().isCanPerform()) {
                logCollector.addLine("weight " + head + " == " + head.getCmLine().getProperties().getWeight());
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
            cmLine.getFlags().setStart(true);
            logCollector.addLine("start " + cmLine.getCommand());
            /**/
            //todo Заменить на вызов модуля
            Process pr = Runtime.getRuntime().exec(cmLine.getCommand());
            while (pr.isAlive()) {
                try {
                    pr.waitFor();
                    BufferedReader in = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        logCollector.addLine("Error stream:");
                        while ((inputLine = in.readLine()) != null) {
                            logCollector.addLine(inputLine);
                        }
                        logCollector.addLine("");
                    }
                    in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    inputLine = in.readLine();
                    if (inputLine != null) {
                        logCollector.addLine("Input stream:");
                        while ((inputLine = in.readLine()) != null) {
                            logCollector.addLine(inputLine);
                        }
                        logCollector.addLine("");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            /**/
            boolean rez = (pr.exitValue() == cmLine.getProperties().getCorrectReturnValue());
            if (!rez) {
                cmLine.getFlags().setCanPerform(false);
                logCollector.addLine("incorrect return value " + cmLine.getCommand());
            } else {
                logCollector.addLine("correct return value " + cmLine.getCommand());
                rubbishCollector.addRubbish(cmLine);
            }
            if (!cmLine.getProperties().isNullFilesMarks()) {
                for (Map.Entry entry : cmLine.getProperties().getFilesMarks().entrySet()) {
                    if ((Integer) entry.getKey() == pr.exitValue()) {
                        File newFile = new File((String) entry.getValue());
                        if (newFile.createNewFile()) {
                            logCollector.addLine("create fileMarks: " + entry.getValue());
                            rubbishCollector.addRubbish((String) entry.getValue(), cmLine);
                            break;
                        } else {
                            logCollector.addLine("error create fileMarks: " + entry.getValue());
                            rez = false;
                        }
                    }
                }
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
                System.err.println("error branch");
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
        try {
            Parser parser = new Parser();
            parser.parameters.addParameters(args);
            if (args.length == 0) {
                if (!view(parser)) {
                    System.exit(1);
                }
            }
            if (parser.start()) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println("<Parser>" + e.toString());
            System.exit(2);
        }
        //HashMap<String, Integer> hashMap = new HashMap<>();

    }

    private static boolean view(Parser parser) throws IOException {
        System.out.println("<Parser>: введите парамметры, для работы. Для запуска системы введите -s, для справки -h, для выхода -e");
        while (true) {
            BufferedReader streamIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("<Parser>: ");
            String[] inPar = streamIn.readLine().split(" ");
            boolean flg_o = false;
            boolean flg_s = false;
            for (String str : inPar) {
                switch (str) {
                    case "-h":
                    case "-H":
                    case "--h":
                    case "-help":
                    case "--help":
                        parser.parameters.printHelp();
                        break;
                    case "-o":
                        flg_o = true;
                        break;
                    case "-t":
                        parser.parameters.time = true;
                        break;
                    case "-m":
                        parser.parameters.memory = true;
                        break;
                    case "-s":
                        flg_s = true;
                        break;
                    case "--exit":
                    case "-exit":
                    case "-e":
                        System.exit(0);
                    default:
                        if (flg_o) {
                            parser.parameters.addFileOut(str);
                            flg_o = false;
                        } else {
                            parser.parameters.addFileCM(str);
                        }
                        break;
                }
                if (flg_s) {
                    break;
                }
            }
            flg_o = false;
            if (flg_s) {
                if (parser.parameters.getNamesFileCM().size() == 0 || parser.parameters.getNamesFileOut().size() == 0) {
                    System.out.println("<Parser>: Не указан fileOut или fileCM. Работа не возможна.");
                    return false;
                }
                break;
            }
        }
        return true;
    }

    class Parameters {
        /* Хранит в себе все параметры,
        *
        * */
        private boolean time = false;
        private boolean memory = false;
        private ArrayList<String> namesFileCM;
        private ArrayList<String> namesFileOut;

        Parameters() {
            namesFileCM = new ArrayList<>();
            namesFileOut = new ArrayList<>();
        }

        void addParameters(String[] args) throws IOException {
            boolean flg_o = false;
            for (String str : args) {
                switch (str) {
                    case "-h":
                    case "-H":
                    case "--h":
                    case "help":
                        printHelp();
                        break;
                    case "-o":
                        flg_o = true;
                        break;
                    case "-t":
                        time = true;
                        break;
                    case "-m":
                        memory = true;
                        break;
                    default:
                        if (flg_o) {
                            addFileOut(str);
                            flg_o = false;
                        } else {
                            addFileCM(str);
                        }
                        break;
                }
            }
        }

        void addFileCM(String nameFile) {
            namesFileCM.add(nameFile);
        }

        void addFileOut(String nameFile) {
            namesFileOut.add(nameFile);
        }

        boolean isTime() {
            return time;
        }

        boolean isMemory() {
            return memory;
        }

        ArrayList<String> getNamesFileCM() {
            return namesFileCM;
        }

        ArrayList<String> getNamesFileOut() {
            return namesFileOut;
        }

        void printHelp() {
            System.out.println("<Parser>: Для работы parser'а доступны следующие входные параметры:");
            System.out.println("<Parser>: -t  — Найти оптимальный путь по минимальному времени");
            System.out.println("<Parser>: -m  — Найти оптимальный путь по минимальному расходу памяти");
            System.out.println("<Parser>: -s  — запуск системы");
            System.out.println("<Parser>: <nameFileCM>     — установить файл вычислительной модели");
            System.out.println("<Parser>: -o <nameFileOut> — установить выходной файл работы системы");
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
