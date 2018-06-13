package org.millburn.kiosk;

import org.millburn.kiosk.db.Database;
import org.millburn.kiosk.exception.InvalidServerStateException;
import org.millburn.kiosk.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server{
    private static Logger logger = new Logger();
    private static Server server;

    private Database database;
    private State state = State.INITIALIZING;
    private boolean test = false;

    private Map<Integer, Transaction> transactions;

    private Server(){
        database = Database.getDatabase("jdbc:mysql://localhost:3306/test", "javster101", "maligna101");
        transactions = new ConcurrentHashMap<>();
    }

    public void run(){
        state = State.RUNNING;
        try{
            var results = database.requestQuery("SELECT * FROM testtable");
            Thread.sleep(5000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static boolean initialize(){
        server = new Server();
        return true;
    }

    private static void loadConfigs(String path){
        var configdir = new File(path);
        var allconfigs = recursiveLoadConfigs(configdir);
        for(var config : allconfigs){
            try{
                Configuration.load(config);
            }catch(IOException e){
                logger.warn("Failed to load configuration file at " + config.getAbsolutePath());
            }
        }
    }

    private static List<File> recursiveLoadConfigs(File directory){
        var allfiles = directory.listFiles();
        var allcfgs = new ArrayList<File>();
        for (var file : allfiles) {
            if (file.isFile()) {
                if(file.getAbsolutePath().contains(".ini")){
                    allcfgs.add(file);
                }
            } else if(file.isDirectory()) {
                recursiveLoadConfigs(file);
            }
        }
        return allcfgs;
    }

    public static Server getCurrent(){
        if(server == null) throw new InvalidServerStateException();
        return server;
    }

    public State getState(){
        return state;
    }

    public boolean isTesting(){
        return test;
    }

    public enum State{
        INITIALIZING, RUNNING, CLOSED, CRASH;
    }
}
