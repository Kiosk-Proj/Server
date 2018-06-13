package org.millburn.kiosk.logging;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogMessage {
    private Class sender;
    private String message;
    private Instant time;
    private Priority priority;

    public LogMessage(Class sender, Priority priority, String message) {
        this.sender = sender;
        this.message = message;
        this.priority = priority;
        this.time = Instant.now();
    }

    public Class getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Priority getPriority() {
        return priority;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "[" + time.atZone(ZoneId.of("America/New_York")).format(DateTimeFormatter.RFC_1123_DATE_TIME) + "] " + priority + " " + sender.getSimpleName() + ": " + message;
    }

    public enum Priority {
        SEVERE, WARNING, INFO, DEBUG;

        public int getPriorityNumber(){
            switch(this){
                case DEBUG: return 0;
                case INFO: return 1;
                case WARNING: return 2;
                default: return 3;
            }
        }
    }
}
