package main.java.ru.compiler;

import main.java.ru.bricks.OrientedBipartiteGraph;
import main.java.ru.bricks.Procedure;

import java.io.BufferedInputStream;
import java.util.Collection;

/** 0) считывание файла
 *  1) Выделение всех возможных ветвлений, на основании результирующих файлов
 *  2) создание графа зависимостей
 */
public class CompilerCMFile {

    public void loadData(String nameFile, Collection<String> heads) {

    }

    public OrientedBipartiteGraph<Procedure,String> getData() {
        return new OrientedBipartiteGraph<Procedure,String>();
    }
}
