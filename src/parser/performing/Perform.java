package parser.performing;

import computationalModel.line.CMLine;
import parser.LogCollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by BODY on 04.07.2017.
 */
public abstract class Perform {

    public Process start(CMLine line, LogCollector logCollector) throws IOException {
        logCollector.addLine("start " + line.getCommand());
        Process pr = start(line);
        while (pr.isAlive()) {
            try {
                pr.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        return pr;
    }

    abstract Process start(CMLine line);
}
