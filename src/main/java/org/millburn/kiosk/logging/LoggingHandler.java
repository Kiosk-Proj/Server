package org.millburn.kiosk.logging;

import java.util.ArrayList;
import java.util.List;

public class LoggingHandler {
    private static List<MessageHandler> receivers = new ArrayList<>();

    public static void initialize(){
        receivers.add(new FileLogger());
        receivers.add(new ConsoleLogger());
    }

    public static void receive(LogMessage message){
        for(var receiver : receivers){
            //if(receiver.getPriority() <= message.getPriority()) receiver.receive(message);
            receiver.receive(message);
        }
    }
}
