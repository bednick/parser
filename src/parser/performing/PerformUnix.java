package parser.performing;

import computationalModel.line.CMLine;

import java.io.IOException;

/**
 * Created by Anastasia on 04.07.17.
 */
public class PerformUnix extends Perform {

    @Override
    Process start(CMLine line) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", line.getCommand());
        return processBuilder.start();
    }

    public static void main(String[] args) throws IOException {
        String s = "file_1 file_2;out.txt;echo \"Hello world\" >> out.txt";
        CMLine cmLine = new CMLine(s, null);
        PerformUnix performUnix = new PerformUnix();
        performUnix.start(cmLine);

    }
}
