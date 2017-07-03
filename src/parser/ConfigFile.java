package parser;

import computationalModel.file.CMFile;

/**
 * Класс отвечает за конфиг файл, в котором хранится информация о предыдущих запусках parser'а
 */
public class ConfigFile {
    private String nameConfigFile;

    public ConfigFile(String nameConfigFile) {
        this.nameConfigFile = nameConfigFile;
    }
    public void setNameConfigFile(String nameConfigFile) {
        this.nameConfigFile = nameConfigFile;
    }
    public void updateConfigFile(CMFile cmFile, LogCollector log){

    }
    public void updateCMFile(CMFile cmFile, LogCollector log){

    }
}
