package parser;

import computationalModel.file.CMFile;
import computationalModel.line.CMLine;
import computationalModel.tree.CMTree;
import computationalModel.tree.CMTreeVertex;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Основной класс
 *
 */
public class Parser {
    private Parameters parameters;
    private volatile Lighthouse lighthouse;
    private CMFile cmFile;
    private LogCollector logCollector;
    private RubbishCollector rubbishCollector;
    private ConfigFile configFile;


    public Parser(String[] args) {
        this.parameters = new Parameters(args);
        this.lighthouse = new Lighthouse();
        this.cmFile  = new CMFile();
        this.logCollector = new LogCollector();
        this.rubbishCollector = new RubbishCollector(this.logCollector);
        this.configFile = new ConfigFile("");
    }
    public boolean start() throws IOException {
        /*
        * Основная логика parser'а
        * */
        try {
            logCollector.addLine("\nSTART PARSER " + new java.util.Date().toString ());
            if(parameters.namesFileOut.size() == 0){
                return true;
            }

            for(String str: parameters.getNamesFileCM()) {//считываем все входные CM
                cmFile.readFile(str);
                logCollector.addLine("NAME CM '" + str + "'");
            }
            configFile.updateCMFile(cmFile);
            for (String nameOut : parameters.getNamesFileOut()) {
                CMTree tree = new CMTree(cmFile, nameOut);

                do{
                    logCollector.push();
                    boolean isCanPerform = false;
                    for (CMTreeVertex ver: setMinWeight(tree).getHead()) { // Выставляем веса!
                        if(ver.getCmLine().getFlags().isCanPerform()){
                            isCanPerform = true;
                            break;
                        }
                    }
                    logCollector.addLine("set tree");
                    logCollector.addLine(tree.toString());
                    if(!isCanPerform){
                        System.out.println("file '" + nameOut + "' can not get!");
                        return false;
                    }
                    logCollector.push();
                }while(!performMinBranch(tree));
            }
            return true;
        } finally {
            rubbishCollector.clear(parameters.getNamesFileOut());
            logCollector.addLine("\nFINISH PARSER " + new java.util.Date().toString ());
            logCollector.push();
        }
    }

    private CMTree setMinWeight(CMTree tree){
        /*
        * выставляет у каждой вершины её вес(зависит от параметров Parser'a)
        * если вершина является недоступной, помечает это
        * */
        Queue<CMTreeVertex> queue = new LinkedList<CMTreeVertex>(); // Для правильного добавления в стек
        Stack<CMTreeVertex> stack = new Stack<CMTreeVertex>();      // Последовательность всех вершин, так чтобы при доставании элемента
                                                                    // у всех входящих вершин были выставлены веса
        boolean isTime = ( parameters.isTime() && !parameters.isMemory())  //определяем, по какому критерию ищем min вес
                      || (!parameters.isTime() && !parameters.isMemory());
        for(CMTreeVertex head: tree.getHead()){
            queue.add(head);
            stack.add(head);
        }
        CMTreeVertex vertex;
        while((vertex = queue.poll()) != null){ //заполняем стек
            for (String nameIn: vertex.getCmLine().getIn()) {
                for (CMTreeVertex inVertex : vertex.getIn(nameIn)) {
                    if(stack.contains(inVertex)) {
                        stack.remove(inVertex);
                    }
                    queue.add(inVertex);
                    stack.add(inVertex);
                }
            }
        }
        while(!stack.empty()){
            vertex = stack.pop();      //берём эмелент и находим минимальный вес
            if(vertex.getCmLine().getFlags().isCanPerform()) { // при условии, что эту строчку можно выполнить
                int minWeightVertex = 0;
                vertex.getCmLine().getProperties().setWeight(0);
                for (String nameIn : vertex.getCmLine().getIn()) {
                    int minWeightFile = Integer.MAX_VALUE;      //минимальный вес, во всех входящих строчках, для ОТДЕЛЬНОГО ВХОДНОГО ФАЙЛА
                    CMTreeVertex minInCMTreeVertex = null;
                    if((new File(nameIn).exists())) {   // если такой файл существует
                        minWeightFile = 0;
                    } else {
                        for (CMTreeVertex inVertex : vertex.getIn(nameIn)) {
                            if (inVertex.getCmLine().getFlags().isCanPerform()) {
                                if(isTime){
                                    if(inVertex.getCmLine().getProperties().getWeightTime() < minWeightFile){
                                        minWeightFile = inVertex.getCmLine().getProperties().getWeightTime();
                                        minInCMTreeVertex = inVertex;
                                    }
                                } else {
                                    if(inVertex.getCmLine().getProperties().getWeightTime() < minWeightFile){
                                        minWeightFile = inVertex.getCmLine().getProperties().getWeightMemory();
                                        minInCMTreeVertex = inVertex;
                                    }
                                }
                            }
                        }
                    }
                    if(minWeightFile == Integer.MAX_VALUE){
                        vertex.getCmLine().getFlags().setCanPerform(false);
                    }
                    minWeightVertex += minWeightFile;
                    vertex.setMinInVertex(nameIn, minInCMTreeVertex);
                }
                vertex.getCmLine().getProperties().setWeight(minWeightVertex);
            }
        } // у всех вершин выставлен вес
        return tree;
    }
    private boolean performMinBranch(CMTree tree) throws IOException {
        CMTreeVertex minVertexHead = null;
        int minWeight = Integer.MAX_VALUE;
        for (CMTreeVertex head: tree.getHead()) {
            if(head.getCmLine().getFlags().isCanPerform()){
                if(head.getCmLine().getProperties().getWeight() < minWeight){
                    minVertexHead = head;
                    minWeight = head.getCmLine().getProperties().getWeight();
                }
            }
        }
        ArrayList<CMLine> branch = new ArrayList<>();
        Queue<CMTreeVertex> queue = new LinkedList<CMTreeVertex>();
        queue.add(minVertexHead);
        CMTreeVertex vertex;
        while((vertex = queue.poll()) != null){
            if(!branch.contains(vertex.getCmLine())) {
                branch.add(vertex.getCmLine());
            }
            for (String nameIn: vertex.getCmLine().getIn()) {
                if(vertex.getMinIn(nameIn) != null) {
                    queue.add(vertex.getMinIn(nameIn));
                }
            }
        }

        CMFile cmFile = new CMFile(branch);
        logCollector.addLine("BRANCH:");
        logCollector.addLine(cmFile.toString());
        return performCMFile(cmFile);
    }

    private boolean performCMLine(CMLine cmLine) {
        try {
            cmLine.getFlags().setStart(true);
            logCollector.addLine("start " + cmLine.getCommand());
            Process pr = Runtime.getRuntime().exec(cmLine.getCommand());
            while (pr.isAlive()) {
                try {
                    pr.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            boolean rez = ( pr.exitValue() == cmLine.getProperties().getCorrectReturnValue() );
            if(!rez){
                cmLine.getFlags().setCanPerform(false);
                logCollector.addLine("incorrect return value " + cmLine.getCommand());
            } else {
               rubbishCollector.addRubbish(cmLine);
            }
            return rez;
        } catch (IOException e) {
            logCollector.addLine(e.toString());
            //System.err.println(e.toString());
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
        while(flag){

            for (CMLine line : branch.getLines()) {
                if(line.getFlags().isStart()){ // если строка уже была запушенна ранее, то и проверять её не нужно более
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
                if(canPerform) {
                    lighthouse.increment();
                    line.getFlags().setStart(true);
                    CompletableFuture<Void> start = CompletableFuture.completedFuture(performCMLine(line))
                            .thenAcceptAsync((x)-> {
                                if(!x) {
                                    lighthouse.setError(true);
                                }
                                synchronized (lighthouse) {
                                    --lighthouse.count;
                                    lighthouse.notifyAll();
                                }
                            });
                    ++count;
                }
                if(count == branch.getSize()){// TODO: 24.10.2016 Реализовать правильную проверку на завершения вычисления ветви 
                    flag = false;
                }
            }
            if(count == 0){
                System.err.println("error brach");
                logCollector.addLine("error brach");
                return false;
            }
            synchronized (lighthouse) {
                if(lighthouse.count != 0) {
                    try {
                        lighthouse.wait();
                    } catch (InterruptedException e) {
                        System.err.println(e.toString());
                    }
                }
                if(lighthouse.error) {

                    while(lighthouse.count > 0) {
                        try {
                            lighthouse.wait();
                        } catch (InterruptedException e) {
                            System.err.println(e.toString());
                        }
                    }
                    return false;
                } else{
                    //System.err.println(" not lighthouse.error");
                }
            }
        }

        return true;
    }

    public static void main(String[] args) {
        try {
            Parser parser = new Parser(args);
            if(parser.start()){
                System.exit(0);
            } else {
                System.exit(1);
            }
        } catch (IOException e){
            System.err.println(e.toString());
            System.exit(2);
        }
        //HashMap<String, Integer> hashMap = new HashMap<>();

    }

    public class Parameters{
        /* Хранит в себе все параметры,
        *
        * */
        private boolean time = false;
        private boolean memory = false;
        private ArrayList<String> namesFileCM;
        private ArrayList<String> namesFileOut;

        public Parameters(String[] args){
            boolean flg_o = false;
            namesFileCM = new ArrayList<>();
            namesFileOut = new ArrayList<>();
            for (String str: args) {
                switch (str){
                    case "-h":
                    case "-H":
                    case "--h":
                    case "help":
                        System.out.println("HELP");
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
                        if(flg_o){
                            namesFileOut.add(str);
                            flg_o = false;
                        } else {
                            namesFileCM.add(str);
                        }
                        break;
                }
            }
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
    public class Lighthouse{
        /*
        * Вспомогательный класс, используется потоками для общения между собой
        * Хранит в себе количество выполняемых задач, и метку
        * указывающую на то, что во время выполнения одной из задач произошла ошибка
        * */
        private int count;
        private volatile boolean error;
        public Lighthouse(){
            count = 0;
        }
        public synchronized void increment(){
            count++;
        }
        public synchronized void decrement(){
            count--;
        }
        public synchronized void setError(boolean error) {
            this.error = error;
        }
    }
}
