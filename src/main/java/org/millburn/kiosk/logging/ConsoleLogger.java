package org.millburn.kiosk.logging;

public class ConsoleLogger extends MessageHandler {
    @Override
    public void receive(LogMessage message) {
        System.out.println(message);
    }
}
