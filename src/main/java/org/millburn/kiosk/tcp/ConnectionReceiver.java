package org.millburn.kiosk.tcp;

import org.millburn.kiosk.logging.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionReceiver implements Runnable{
    public static final int PORT = 94828;
    private static ConnectionReceiver receiversingleton;
    private static Thread receiverthread;
    private static Logger logger = new Logger();

    private ServerSocket socket;

    private ConnectionReceiver(){
        try{
            socket = new ServerSocket(PORT);
            logger.info("Initialized listening server on port " + PORT);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static ConnectionReceiver get(){
        if(receiversingleton == null) return create();
        else return receiversingleton;
    }

    private static ConnectionReceiver create(){
        var receiver = new ConnectionReceiver();
        var receiverthread = new Thread(receiver);

        receiverthread.setDaemon(true);
        receiverthread.setName("ConnectionListener");
        receiverthread.setUncaughtExceptionHandler((th, ex) ->
                logger.severe("Connection listener thread crashed due to exception: " + ex.getMessage()));

        receiverthread.start();

        return receiver;
    }

    public static Thread getListenerThread(){
        return receiverthread;
    }

    @Override
    public void run(){

    }
}
