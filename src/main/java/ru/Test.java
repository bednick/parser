package main.java.ru;


import main.java.ru.demons.EnvironmentalController;
import main.java.ru.demons.LogCollector;
import main.java.ru.demons.RubbishCollector;
import main.java.ru.preprocessor.Preprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by BODY on 15.10.2017.
 */
public class Test {
    private static Logger log = Logger.getLogger(Test.class.getName());
    public static void main(String[] args) {
        /*EnvironmentalController environmentalController = new EnvironmentalController();
        Preprocessor preprocessor = new Preprocessor();
        try {
            List<String> lines = preprocessor.readFile(environmentalController.getModel("cm/test.cm"));
            lines.forEach(System.out::println);
            System.out.println(lines.size());

        } catch (IOException e) {
            e.printStackTrace();
//        }*/

        LogCollector collector = new LogCollector();
        EnvironmentalController environmentalController = new EnvironmentalController();
        try {
            environmentalController.createModel("TestModel");
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Test 1");
        RubbishCollector rubbishCollector = new RubbishCollector();
        rubbishCollector.add(new File("TestModel"));
        System.out.println( environmentalController.isPresent("TestModel"));
        rubbishCollector.clear();
        System.out.println( environmentalController.isPresent("TestModel"));
        log.info("Test 2");
        log.info("Test 3");
    }
}
