package parser.performing;

import computationalModel.line.CMLine;

import java.io.IOException;

/**
 * Created by Anastasia on 04.07.17.
 */
public class PerformUnix extends Perform {

    @Override
    Process start(CMLine line) throws IOException {
        /*
        *  Первый аргумент возможен "sh", "csh"
        * */
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", line.getCommand());
        return processBuilder.start();
    }

    public static void main(String[] args) throws IOException {
        String s = ";file_ssh_yes ;ssh hpcuser29@clu.nusc.ru -i /Users/Anastasia/key_nusc_school_2017.dat";
        CMLine cmLine = new CMLine(s, null);
        PerformUnix performUnix = new PerformUnix();
        performUnix.start(cmLine);

    }
}
