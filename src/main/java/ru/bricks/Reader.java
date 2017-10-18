package main.java.ru.bricks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reader {
    public final static String START_COMMENT = "##";
    public final static String EXTEND_LINE = "\\";

    public List<String> readFile(String nameFile) throws IOException {
        return lineJoin(Files.lines(Paths.get(nameFile), StandardCharsets.UTF_8)
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
                joinLines.clear();
            }
        }
        if (builder.length() != 0) {
            joinLines.add(builder.toString());
        }
        return joinLines;
    }
}
