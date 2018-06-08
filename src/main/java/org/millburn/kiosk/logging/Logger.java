package org.millburn.kiosk.logging;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

public class Logger {
    private Class source;

    public Logger(Class clazz){
        this.source = clazz;
    }

    public Logger(){
        this(StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass());
    }

    public void log(LogMessage.Priority priority, String message){
        LoggingHandler.receive(new LogMessage(source, priority, message));
    }

    public void warn(String message){
        LoggingHandler.receive(new LogMessage(source, LogMessage.Priority.WARNING, message));
    }

    public void severe(String message){
        LoggingHandler.receive(new LogMessage(source, LogMessage.Priority.SEVERE, message));
    }

    public void info(String message){
        LoggingHandler.receive(new LogMessage(source, LogMessage.Priority.INFO, message));
    }

    public void debug(String message){
        LoggingHandler.receive(new LogMessage(source, LogMessage.Priority.DEBUG, message));
    }
}
