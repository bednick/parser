package main.java.ru;


import main.java.ru.demons.EnvironmentalController;
import main.java.ru.preprocessor.Preprocessor;

import java.io.IOException;
import java.util.List;

/**
 * Created by BODY on 15.10.2017.
 */
public class Test {
    public static void main(String[] args) {
        /*EnvironmentalController environmentalController = new EnvironmentalController();
        Preprocessor preprocessor = new Preprocessor();
        try {
            List<String> lines = preprocessor.readFile(environmentalController.getModel("cm/test.cm"));
            lines.forEach(System.out::println);
            System.out.println(lines.size());

        } catch (IOException e) {
            e.printStackTrace();
        }*/

        EnvironmentalController environmentalController = new EnvironmentalController();
        try {
            environmentalController.createModel("TestModel");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }
}
