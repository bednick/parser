package parser;

import computationalModel.file.CMFile;
import computationalModel.line.CMLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;

/**
 * Created by BODY on 18.10.2016.
 */
public class Parser {
    private Parameters parameters;
    private final Lighthouse lighthouse;
    private CMFile cmFile;
    private LogCollector logCollector;
    private RubbishCollector rubbishCollector;


    public Parser(String[] args) {
        this.parameters = new Parameters(args);
        this.lighthouse = new Lighthouse();
        this.cmFile  = new CMFile();
        this.logCollector = new LogCollector();
        this.rubbishCollector = new RubbishCollector(this.logCollector);
    }
    public void start() throws IOException {
        /*
        * Основная логика parser'а
        * */
        if(parameters.namesFileOut.size() == 0){
            return;
        }
        for(String str: parameters.getNamesFileCM()) {//считываем все входные CM
            cmFile.readFile(str);
        }
        try {

            for (String nameOut : parameters.getNamesFileOut()) {
                boolean errorPerform = false;
                CMFile tree = getTree(cmFile, nameOut);
                CMFile branch;
                do {
                    checkCanPerform(tree);
                    branch = getBranch(tree);
                    logCollector.addLine("selected branch");
                    if (branch.getSize() == 0) {
                        logCollector.addLine("branch is empty");
                        errorPerform = true;
                        break;
                    }
                } while (!performBranch(branch));
                if (errorPerform) {
                    logCollector.addLine("can not be obtained " + nameOut);
                    System.err.println("can not be obtained " + nameOut);
                    return;
                }
            }
        } finally {
            rubbishCollector.clear(parameters.getNamesFileOut());
        }
    }
    public void checkCanPerform(CMFile cmFile) {
        /*
        * Помечает все вершины, которые нельзя получить из CMFile
        * */
        for (CMLine cmLine: cmFile.getLines()) {
            for (String nameIn: cmLine.getIn()) {
                if(cmFile.getOnlyInput().contains(nameIn)){
                    if( !(new File(nameIn).exists()) ){
                        cmLine.getFlags().setCanPerform(false);
                    }
                }
            }
        }
        boolean flag = true;
        while(flag){
            flag = false;
            for (CMLine cmLine: cmFile.getLines()) {
                if( cmLine.getFlags().isCanPerform() ) { // проверяются все, у которых стоит влаг, что их можно получить
                    for (String nameIn : cmLine.getIn()) {
                        if ( ! cmFile.getOnlyInput().contains(nameIn) ) {
                            boolean ifAvailable = false;
                            for (CMLine cmIn : cmFile.getForOut(nameIn)) {
                                if (cmIn.getFlags().isCanPerform()) {
                                    ifAvailable = true;
                                }
                            }
                            if (!ifAvailable) {
                                cmLine.getFlags().setCanPerform(false);
                                flag = true;
                            }
                        }
                    }
                }
            }
        }
    }
    public CMFile getTree(CMFile cmFile, String nameResult) {
        /*
        * Получение дерева зависимостей, по имени вершины
        * С учётом того, что некоторые вершины мы получить не сможем(их мы не добавляем)
        * */
        ArrayList<CMLine> newTree = new ArrayList<>();
        PriorityQueue<String> queueName = new PriorityQueue<>();
        PriorityQueue<CMLine> queueCMLine = new PriorityQueue<>();

        for (CMLine cm: cmFile.getForOut(nameResult)) { //ставим в очередь изначальное множество возможных путей
            if (cm.getFlags().isCanPerform()) {         //при условии что мы ранее не поняли, что получить её не можем
                queueCMLine.add(cm);
                newTree.add(cm);
            }
        }
        while (queueCMLine.size() > 0) {
            CMLine cmLine = queueCMLine.poll();     // достаём из очереди строку с которой будем работать
            for (String str: cmLine.getIn()) {      // Добавляем в очередь все имена, необходимые для получения результата
                if ( ! (new File(str).exists()) ) { // Если это файл, то не добавляем его в очередь (уже получен)
                    if(!queueName.contains(str)) {  //если очередь уже не содержит элемент
                        queueName.add(str);
                    }
                }
            }
            while (queueName.size() > 0){                   //проходим по всем входным файлам
                String name = queueName.poll();             //выбираем из очереди элемент
                for (CMLine cm: cmFile.getForOut(name)) {   //ищем все возможные пути его получения
                    if (cm.getFlags().isCanPerform()) {     //если эту ветвь нельзя исполнить, то не запоминаем
                        if(!queueCMLine.contains(cm)) {     //если очередь уже содержит элемент, то не добавляем её
                            queueCMLine.add(cm);
                            newTree.add(cm);
                        }
                    }
                }
            }

        }
        return new CMFile(newTree);
    }
    public CMFile getBranch(CMFile tree, String nameResult){
        int countColor = 1;
        boolean isTime = (  parameters.isTime() && !parameters.isMemory() )
                      || ( !parameters.isTime() && !parameters.isMemory() );

        //первая итерация алгоритма
        for(CMLine cmLine: tree.getLines()) {
            boolean isFirstLayer = false; // первый слой - команды, во вход. файлах которых есть файл, не получаемый из CM
            for(String fileIn: cmLine.getIn()) {
                if (tree.getOnlyInput().contains(fileIn)) {
                    if( isTime ){ // опт по времени
                        cmLine.getProperties().setWeight(cmLine.getProperties().getWeightTime());
                        cmLine.getProperties().setColor(countColor);
                        ++countColor;

                    } else { // иначе по памяти
                        cmLine.getProperties().setWeight(cmLine.getProperties().getWeightMemory());
                        cmLine.getProperties().setColor(countColor);
                        ++countColor;
                    }
                    isFirstLayer = true;
                    break;
                }
            }
            if(!isFirstLayer) {
                cmLine.getProperties().setWeight(cmLine.getProperties().INFINITEWEIGHT);
            }
        }
        return null;
    }
    public boolean performBranch(CMFile branch) throws IOException {
        boolean flag = true;
        lighthouse.error = false;
        int count = 0; //количесто выполненых операций
        while(flag){
            for (CMLine line : branch.getLines()) {
                if(line.getFlags().isStart()){ // если строка уже была запушенна ранее, то и проверять её не нужно более
                    continue;
                }
                boolean canPerform = true;
                for (String nameIn : line.getIn()) {//Проверяем, можем ли выполнить команду
                    if (!(new File(nameIn).exists())) {
                        canPerform = false;
                        break;
                    }
                }
                if(canPerform) {
                    lighthouse.increment();
                    CompletableFuture<Void> start = CompletableFuture.completedFuture(performCMLine(line))
                            .thenAcceptAsync((x)-> {
                                if(!x) {
                                    synchronized (lighthouse) {
                                        lighthouse.error = true;
                                        --lighthouse.count;
                                        lighthouse.notifyAll();
                                    }
                                } else {
                                    lighthouse.decrement();
                                }

                            });
                    ++count;
                }
                if(count == branch.getSize()){
                    flag = false;
                }
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
                }
            }
        }

        return true;
    }
    public boolean performCMLine(CMLine cmLine) throws IOException {
        try {
            logCollector.addLine("start " + cmLine.getCommand());
            cmLine.getFlags().setStart(true);
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
            }
            return rez;
        } finally {
            cmLine.getFlags().setFinish(true);
            logCollector.addLine("finish " + cmLine.getCommand());
        }
    }
    public static void main(String[] args) {
        try {
            Parser parser = new Parser(args);
            parser.start();
        } catch (IOException e){
            System.err.println(e.toString());
            System.exit(-1);
        }
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
    }
}
