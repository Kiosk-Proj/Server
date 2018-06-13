package org.millburn.kiosk.logging;

public class ConsoleLogger extends MessageHandler {
    public ConsoleLogger(LogMessage.Priority priority){
        super(priority);
    }

    @Override
    public void receive(LogMessage message) {
        System.out.println(message);
    }
}
