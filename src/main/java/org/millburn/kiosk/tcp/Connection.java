package org.millburn.kiosk.tcp;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.time.Instant;

public abstract class Connection implements Runnable{
    public static final int KIOSK = 1, TABLET = 2;

    private Socket socket;
    private Instant time;
    private int type;
    private int id;

    public Connection(Socket socket, int type, int id){
        this.socket = socket;
        this.time = Instant.now();
        this.type = type;
        this.id = id;
    }

    public Socket getSocket(){
        return socket;
    }

    public Instant getConnectionTime(){
        return time;
    }

    public int getType(){
        return type;
    }

    public int getId(){
        return id;
    }

    @Override
    public void run(){

    }

    public abstract void runInternal() throws IOException, SQLException;

}
