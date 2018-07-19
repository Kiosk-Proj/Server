package org.millburn.kiosk.logging;

import java.util.ArrayList;
import java.util.List;

public class LoggingHandler {
    private static List<MessageHandler> receivers = new ArrayList<>();

    public static void initialize(){
        receivers.add(new FileLogger(LogMessage.Priority.WARNING));
        receivers.add(new ConsoleLogger(LogMessage.Priority.DEBUG));
    }

    public static void receive(LogMessage message){
        for(var receiver : receivers){
            if(receiver.getPriority().getPriorityNumber() <= message.getPriority().getPriorityNumber()) receiver.receive(message);
        }
    }
}
