package parser.view;

import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by BODY on 10.07.2017.
 */
public class ViewConsole extends View {
    private final static String prefix = "<Parser>: ";
    private boolean newLine;
    private Parser.Parameters parameters;

    public ViewConsole() {
        this.newLine = true;
    }

    @Override
    public void start(String[] args) throws IOException {
        parameters = new Parser.Parameters();
        if (args.length != 0) {
            setParameters(parameters, args);
        } else {
            if (!considerConsole()) {
                System.exit(1);
            }
        }
    }

    private boolean considerConsole() throws IOException {
        println("введите парамметры, для работы. Для запуска системы введите -s, для справки -h, для выхода -e");
        while (true) {
            BufferedReader streamIn = new BufferedReader(new InputStreamReader(System.in));
            print("");
            String[] inPar = streamIn.readLine().split(" ");
            // todo stream
            boolean flg_o = false;
            boolean flg_s = false;
            for (String str : inPar) {
                switch (str) {
                    case "-h":
                    case "-H":
                    case "--h":
                    case "-help":
                    case "--help":
                        printHelp();
                        break;
                    case "-o":
                        flg_o = true;
                        break;
                    case "-t":
                        parameters.setTime(true);
                        break;
                    case "-m":
                        parameters.setMemory(true);
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
                            parameters.addFileOut(str);
                            flg_o = false;
                        } else {
                            parameters.addFileCM(str);
                        }
                        break;
                }
                if (flg_s) {
                    break;
                }
            }
            if (flg_s) {
                if (parameters.getNamesFileCM().size() == 0 || parameters.getNamesFileOut().size() == 0) {
                    println("Не указан fileOut или fileCM. Работа не возможна.");
                    return false;
                }
                break;
            }
        }
        return true;
    }

    public void setParameters(Parser.Parameters parameters, String[] args) throws IOException {
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
                    parameters.setTime(true);
                    break;
                case "-m":
                    parameters.setMemory(true);
                    break;
                default:
                    if (flg_o) {
                        parameters.addFileOut(str);
                        flg_o = false;
                    } else {
                        parameters.addFileCM(str);
                    }
                    break;
            }
        }
    }

    @Override
    public Parser.Parameters getParameters() {
        return parameters;
    }

    @Override
    public void print(String message) {
        if (newLine) {
            System.out.print(prefix);
        }
        System.out.print(message);
        newLine = false;
    }

    @Override
    public void println(String message) {
        if (newLine) {
            System.out.print(prefix);
        }
        System.out.println(message);
        newLine = true;
    }

    @Override
    public void printError(String message) {
        println(message);
    }

    @Override
    public void printHelp() {
        println("Для работы parser'а доступны следующие входные параметры:");
        println("-t  — Найти оптимальный путь по минимальному времени");
        println("-m  — Найти оптимальный путь по минимальному расходу памяти");
        println("-s  — запуск системы");
        println("<nameFileCM>     — установить файл вычислительной модели");
        println("-o <nameFileOut> — установить выходной файл работы системы");
    }


}
