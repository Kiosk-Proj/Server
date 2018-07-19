package org.millburn.kiosk.tcp;

import org.millburn.kiosk.logging.Logger;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.time.Instant;

public abstract class Connection implements Runnable{
    public static final int KIOSK = 0, TABLET = 1;

    private Socket socket;
    private Instant time;
    private int type;
    private int id;

    protected Logger log = new Logger();

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
        try {
            runInternal();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract void runInternal() throws IOException, SQLException;

}
