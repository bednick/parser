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
        if(parameters.namesFileOut.size() == 0){
            return;
        }
        for(String str: parameters.getNamesFileCM()) {
            cmFile.readFile(str);
        }
        CMFile tree = getTree(cmFile, "");
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
    public boolean performBranch(CMFile branch) throws IOException {
        boolean flag = true;
        lighthouse.error = false;
        int count = 0;
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
            boolean rez = pr.exitValue() == cmLine.getProperties().getCorrectReturnValue();
            if(!rez){
                cmLine.getFlags().setCanPerform(false);
            }
            return rez;
        } finally {
            cmLine.getFlags().setFinish(true);
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
        private int count;
        private volatile boolean error;
        public Lighthouse(){
            count = 0;
        }
        public synchronized int getCount(){
            return count;
        }
        public synchronized void increment(){
            count++;
        }
        public synchronized void decrement(){
            count--;
        }
    }
}
