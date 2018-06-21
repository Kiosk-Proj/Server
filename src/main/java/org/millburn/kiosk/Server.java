package org.millburn.kiosk;

import org.millburn.kiosk.db.Database;
import org.millburn.kiosk.exception.InvalidServerStateException;
import org.millburn.kiosk.logging.Logger;
import org.millburn.kiosk.tcp.Connection;

import java.io.File;
import java.io.IOException;
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

    private Map<Long, Transaction> transactions;
    private List<Connection> kiosks = new ArrayList<>();
    private List<Connection> tablets = new ArrayList<>();

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
        }
    }

    public static boolean initialize(){
        server = new Server();
        Executor.initialize();
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

    public Transaction createTransaction(int userid, int kiosk, String name){
        var transaction = new Transaction(userid, kiosk, name);
        this.transactions.put(transaction.getTransactionId(), transaction);
        return transaction;
    }

    public Transaction getTransaction(long transid){
        return transactions.get(transid);
    }

    public void processTransaction(Transaction transaction){

    }

    public void addKiosk(Connection conn){
        this.kiosks.add(conn);
    }

    public void addTablets(Connection conn){
        this.tablets.add(conn);
    }

    public Database getDatabase(){
        return database;
    }

    public enum State{
        INITIALIZING, RUNNING, CLOSED, CRASH;
    }
}
