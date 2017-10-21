package main.java.ru.demons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by BODY on 15.10.2017.
 */
public class LogCollector {

    private static final LogManager logManager = LogManager.getLogManager();
    private static final Logger LOGGER = Logger.getLogger("configureLogger");

    static{
        try {
            logManager.readConfiguration(new FileInputStream("settings/logging.properties"));
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Error in loading configuration",exception);
        }
    }

}
