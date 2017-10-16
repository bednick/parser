package main.java.ru.old.parser.performing;

import main.java.ru.old.computationalModel.line.CMLine;

import java.io.IOException;

/**
 * Created by BODY on 04.07.2017.
 */
public class PerformWin extends Perform {
    @Override
    Process start(CMLine line) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", line.getCommand());
        return processBuilder.start();
    }

    @Override
    public String getCharsetName() {
        return "cp866";
    }
}
