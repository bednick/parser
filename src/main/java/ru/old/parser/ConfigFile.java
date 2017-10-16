package main.java.ru.old.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс отвечает за конфиг файл, в котором хранится информация о:
 * path_cm - пути о местах, в которых дополнительно к окружению нужно искать файлы *.cm
 * path_environment
 * path_parser
 * default_weight - вес не помеченных вершин(не зависит он типа веса)
 * ...
 */
public class ConfigFile {
    private static final String NAME_FILE = "settings" + System.getProperty("file.separator") + "configuration.txt";

    private static final String SPECIAL_PATH_SYMBOL = "$";
    private static final String NAME_PATH_ENVIRONMENT = "path_environment";
    private static final String NAME_PATH_PARSER = "path_parser";

    private static final String NAME_SET_PATH_CM = "path_cm";
    private static final String NAME_SET_DEFAULT_WEIGHT = "default_weight";

    private static final String START_COMMENT = "#";
    private static final String DELIMITER_NAME_VALUE = "=";
    private static final String DELIMITER_VALUE_VALUE = ";";

    private LogCollector log;

    private String path_environment;
    private String path_parser;

    private List<String> path_cm;
    private int default_weight = 25;


    public ConfigFile(String path_environment, String path_parser, LogCollector log) {
        this.path_environment = path_environment;
        this.path_parser = path_parser;
        this.log = log;
        this.path_cm = new ArrayList<>();
        path_cm.add(path_environment);
        try {
            readFile();
        } catch (IOException e) {
            log.addLine("ERROR READ CONFIG FILE:");
            log.addLine(e.toString());
        }
    }

    private void readFile() throws IOException {
        String[] names = {
                path_environment + System.getProperty("file.separator") + NAME_FILE,
                path_parser + System.getProperty("file.separator") + NAME_FILE
        };
        for (String name : names) {
            if (readFile(name)) {
                log.addLine("PATH CONFIGURATION: " + name);
                break;
            }
        }
    }

    private boolean readFile(String nameFile) {
        try {
            Files.lines(Paths.get(nameFile), StandardCharsets.UTF_8)
                    .filter(l -> !l.isEmpty() && !l.startsWith(START_COMMENT))
                    .forEach(line -> {
                        if (line.startsWith(NAME_SET_PATH_CM + DELIMITER_NAME_VALUE)) {
                            readPaths(line.substring(NAME_SET_PATH_CM.length() + DELIMITER_NAME_VALUE.length()));
                        } else if (line.startsWith(NAME_SET_DEFAULT_WEIGHT + DELIMITER_NAME_VALUE)) {
                            readDefaultWeight(line.substring(NAME_SET_DEFAULT_WEIGHT.length() + DELIMITER_NAME_VALUE.length()));
                        } else {
                            log.addLine("ERROR LINE CONFIG FILE:");
                            log.addLine(line);
                        }
                    });
        } catch (IOException e) {
            log.addLine(e.toString());
            return false;
        }
        return true;
    }

    private void readDefaultWeight(String line) {
        default_weight = Integer.parseInt(line);
    }

    private void readPaths(String line) {
        String[] paths = line.split(DELIMITER_VALUE_VALUE);
        Arrays.stream(paths).forEach(this::addPath);
    }

    private void addPath(String line) {
        String path = getPath(line);
        File file = new File(path);
        if (file.isDirectory()) {
            path = file.getAbsolutePath();
            if (!path_cm.contains(path)) {
                path_cm.add(path);
                log.addLine("PATH FOUND: " + path);
            }
        } else {
            log.addLine("PATH NOT FOUND: " + file.getName());
        }
    }

    private String getPath(String relativePath) {
        if (relativePath.contains(SPECIAL_PATH_SYMBOL + NAME_PATH_PARSER)) {
            relativePath = relativePath.replace(SPECIAL_PATH_SYMBOL + NAME_PATH_PARSER, path_parser);
        }
        if (relativePath.contains(SPECIAL_PATH_SYMBOL + NAME_PATH_ENVIRONMENT)) {
            relativePath = relativePath.replace(SPECIAL_PATH_SYMBOL + NAME_PATH_ENVIRONMENT, path_environment);
        }
        return relativePath;
    }

    public int getDefaultWeight() {
        return default_weight;
    }

    public List<String> getPathCM() {
        return path_cm;
    }
}
