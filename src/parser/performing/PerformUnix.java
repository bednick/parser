package parser.performing;

import computationalModel.line.CMLine;

import java.io.IOException;

/**
 * Created by Anastasia on 04.07.17.
 */
public class PerformUnix extends Perform {

    @Override
    Process start(CMLine line) {
        Process processUnix = null;
        ProcessBuilder processBuilder = new ProcessBuilder(line.getCommand());
        try {
            processUnix = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processUnix;
    }

    public static void main(String[] args) {
        String s = ";file_2;touch file_2";
        CMLine cmLine = new CMLine(s, null);
        PerformUnix performUnix = new PerformUnix();
        performUnix.start(cmLine);

    }
}
