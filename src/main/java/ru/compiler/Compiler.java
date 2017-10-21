package main.java.ru.compiler;

import main.java.ru.bricks.OrientedBipartiteGraph;
import main.java.ru.bricks.Pair;
import main.java.ru.bricks.Procedure;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 0) считывание файлов
 *  1) Разрешение зависимостей (#include)
 *  2) Раскрутка перечислений (#for)
 *  2) создание графа зависимостей на основе выходных файлов
 */
public class Compiler {
    private Set<Pair<String, String>> define;
    private Map<String, String> include;

    public OrientedBipartiteGraph<Procedure,String> compiling(List<Pair<String,List<String>>> models, Set<String> heads) {
        return new OrientedBipartiteGraph<Procedure,String>();
    }
}
