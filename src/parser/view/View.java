package parser.view;

import parser.Parser;

import java.io.IOException;

/**
 * Created by BODY on 10.07.2017.
 */
public abstract class View {
    protected Parser parser;

    public View() {}

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public abstract void start(String[] args) throws IOException;

    public abstract Parser.Parameters getParameters();

    public abstract void print(String message);

    public abstract void println(String message);

    public abstract void printError(String message);

    public abstract void printHelp();

}