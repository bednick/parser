package main.java.ru.demons;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * использовать одиночку
 */
public class EnvironmentalController {
    private final String PATH_SETTINGS = "settings";
    private Set<String> marks;
    private List<File> pathEnvironment;
    private List<File> pathModels;
    private File pathJar;

    public EnvironmentalController() {
        this.marks = new TreeSet<>();
        this.pathEnvironment = new ArrayList<>();
        this.pathEnvironment.add(new File("."));
        this.pathModels = new ArrayList<>();
        this.pathModels.add(new File("."));
        this.pathJar = new File(EnvironmentalController.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        this.pathModels.add(pathJar);
    }

    public void addPathEnvironment(String path) {
        pathEnvironment.add(new File(path));
    }

    public void addPathEnvironment(File path) {
        pathEnvironment.add(path);
    }

    public void addPathModels(String path) {
        pathModels.add(new File(path));
    }

    public void addPathModels(File path) {
        pathModels.add(path);
    }

    public File getModel(String name) {
        return new File(name);
    }

    public boolean isPresentModel(String name) {
        return "yesModel".equals(name);
    }

    public boolean isPresent(String name) {
        return marks.contains(name) | check(name, pathEnvironment);
    }

    public File createModel(String name) throws IOException {
        String pathNewModel = String.format("%s%s%s", pathEnvironment.get(0).getAbsolutePath(), File.separator, name);
        File newModel = new File(pathNewModel);
        if (!newModel.createNewFile()) {
            if (newModel.isFile()) {
                throw new FileAlreadyExistsException(String.format("file model '%s' already exists", pathNewModel));
            }
            throw new IOException(String.format("file model '%s' can not be created", pathNewModel));
        }
        return newModel;
    }

    public void createMark(String name) {
        marks.add(name);
    }

    private boolean check(String mark, List<File> paths) {
        boolean check = false;
        for (File path: paths) {
            File checkFile = new File(path, mark);
            check |= checkFile.isFile();
        }
        return check;
    }

    public File getFileSettings(String name) {
        File dir = new File(pathJar, PATH_SETTINGS);
        return new File(dir, name);
    }
// А нужно ли?
//    public File get(String name) {
//        for (File dir: pathEnvironment) {
//            File file = new File(dir, name);
//            if (file.isFile()) {
//                return file;
//            }
//        }
//    }
}
