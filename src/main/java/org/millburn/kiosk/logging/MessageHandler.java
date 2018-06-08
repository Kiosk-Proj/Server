package org.millburn.kiosk.logging;

public abstract class MessageHandler {
    private LogMessage.Priority priority = LogMessage.Priority.INFO;

    public abstract void receive(LogMessage message);

    public void setPriority(LogMessage.Priority priority){
        this.priority = priority;
    }

    public LogMessage.Priority getPriority(){
        return priority;
    }
}
