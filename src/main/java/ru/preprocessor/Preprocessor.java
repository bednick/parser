package main.java.ru.preprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс отвечающий за подготовку *.cm файлов
 * 0) Исключение комментариев и лишних пробелов
 */
public class Preprocessor {
    public final static String START_COMMENT = "##";
    public final static String EXTEND_LINE = "\\";

    public Preprocessor() {}

    public List<String> readFile(File file) throws IOException {
        return lineJoin(Files.lines(file.toPath(), StandardCharsets.UTF_8)
                .filter(l->!l.startsWith(START_COMMENT))
                .filter(l->!l.isEmpty())
                .map(l->l.replaceAll("\\s+", " ")));
    }

    private List<String> lineJoin(Stream<String> lines) {
        List<String> joinLines = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String line: lines.collect(Collectors.toList())) {
            builder.append(line);
            if (!line.endsWith(EXTEND_LINE)) {
                joinLines.add(builder.toString());
                builder.setLength(0);
            } else {
                builder.deleteCharAt(builder.length()-1);
            }
        }
        if (builder.length() != 0) {
            joinLines.add(builder.toString());
        }
        return joinLines;
    }
}
