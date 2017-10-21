package main.java.ru.demons;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by BODY on 15.10.2017.
 */
public class RubbishCollector {
    private Logger log = Logger.getLogger( RubbishCollector.class.getName());
    private Set<File> rubbishFile;

    public RubbishCollector() {
        log.log(Level.CONFIG, "Start rubbish collector");
        this.rubbishFile = new HashSet<>();
    }

    public void add(File rubbish) {
        this.rubbishFile.add(rubbish);
    }

    public void add(Collection<File> rubbish) {
        this.rubbishFile.addAll(rubbish);
    }

    public void clear() {
        for(File f: rubbishFile) {
            if (f.isFile()) {
                if (f.delete()) {
                    log.log(Level.FINER, String.format("Delete file '%s'", f.getName()));
                } else {
                    log.log(Level.FINE, String.format("File '%s' not deleted", f.getName()));
                }
            }
        }
        rubbishFile.clear();
    }
}
