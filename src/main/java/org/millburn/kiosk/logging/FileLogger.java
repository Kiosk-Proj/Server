package org.millburn.kiosk.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class FileLogger extends MessageHandler{

    public FileLogger(LogMessage.Priority priority){
        super(priority);
    }

    @Override
    public void receive(LogMessage message) {
        var fname = message.getTime()
                .atZone(ZoneId.of("America/New_York"))
                .toLocalDate()
                .format(DateTimeFormatter.ISO_DATE);

        var path = Paths.get("logs/" + fname + ".log");

        try {
            Files.write(path, Arrays.asList(message.toString()), StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
