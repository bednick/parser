package parser.performing;

import computationalModel.line.CMLine;
import parser.LogCollector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by BODY on 04.07.2017.
 */
public abstract class Perform {

    public Process start(CMLine line, LogCollector logCollector) throws IOException {
        logCollector.addLine("start " + line.getCommand());
        Process pr = start(line);
        while (pr.isAlive()) {
            try {
                pr.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        logStream("Error stream:", pr.getErrorStream(), logCollector);
        logStream("Input stream:", pr.getInputStream(), logCollector);
        return pr;
    }

    private void logStream(String name, InputStream stream, LogCollector logCollector) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        int allBytes = 0;
        while ( (bytesRead = stream.read( buffer )) > 0 ) {
            temp.write( buffer, 0, bytesRead );
            allBytes += bytesRead;
        }
        if (allBytes > 0) {
            logCollector.addLine(name);
            logCollector.addLine(temp.toString(getCharsetName()));
            //logCollector.addLine("");
        }
    }

    public String getCharsetName() {
        return "UTF-8";
    }

    abstract Process start(CMLine line) throws IOException;
}
