package main.java.ru.external.compiler;

import main.java.ru.interior.bricks.OrientedBipartiteGraph;
import main.java.ru.interior.bricks.Procedure;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Created by BODY on 03.10.2017.
 */
public class CompilerCMFile implements ICompiler<BufferedInputStream,OrientedBipartiteGraph<Procedure,String>> {


    @Override
    public void loadData(BufferedInputStream stream, Object[] args) {

    }

    @Override
    public OrientedBipartiteGraph<Procedure,String> getData() {
        return null;
    }
}
