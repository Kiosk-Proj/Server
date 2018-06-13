package org.millburn.kiosk.db;


import org.millburn.kiosk.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DatabaseConnectionPool{
    private static class Lock{}
    private final Lock lock = new Lock();

    private List<DBPair> threads;
    private Queue<DatabaseRequest> requests;

    private String address;
    private String username;
    private String pw;
    private int amount;

    public DatabaseConnectionPool(String address, String username, String pw, int amount){
        this.address = address;
        this.username = username;
        this.pw = pw;
        this.amount = amount;
        requests = new LinkedBlockingQueue<>(100);
    }

    public void run(){
        var threadlist = new ArrayList<DBPair>();

        for(int i = 0; i < amount; i++){
            var dbconnthread = new DatabaseConnectionThread(address, decrypt(username, pw));
            var thread = new Thread(dbconnthread);

            thread.setDaemon(true);
            thread.setName("Database Connection " + i);

            var pair = new DBPair(dbconnthread, thread);

            threadlist.add(pair);
        }

        threads = List.copyOf(threadlist);

        threads.forEach(p -> p.thread.start());
    }

    private DBCredentials decrypt(String username, String password){
        return new DBCredentials(username, password);
    }

    public SQLFuture query(String command){
        DatabaseRequest request = new DatabaseRequest();
        request.value = command;
        request.future = new SQLFuture();

        requests.add(request);
        synchronized(lock){
            lock.notifyAll();
        }

        return request.future;
    }

    private class DBPair{
        DatabaseConnectionThread connection;
        Thread thread;

        public DBPair(DatabaseConnectionThread connection, Thread thread){
            this.connection = connection;
            this.thread = thread;
        }

        public DatabaseConnectionThread getConnection(){
            return connection;
        }

        public Thread getThread(){
            return thread;
        }
    }

    private class DatabaseRequest{
        String value;
        SQLFuture future;
    }

    private class DatabaseConnectionThread implements Runnable{
        private Logger logger = new Logger();
        private Connection conn;
        private volatile boolean run = true;
        private String address;
        private DBCredentials creds;

        public DatabaseConnectionThread(String address, DBCredentials creds){
            this.address = address;
            this.creds = creds;
        }

        @Override
        public void run()  {
            try(Connection connection = DriverManager.getConnection(address, creds.getUsername(), creds.getSinglePassword())){
                while(run){
                    synchronized(lock){
                        while(requests.isEmpty()){
                            try{
                                lock.wait();
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }

                        var request = requests.poll();

                        if(request == null) continue;

                        var statement = connection.createStatement();
                        statement.execute(request.value);
                        request.future.set(new SQLResult(statement.getResultSet()), statement.getUpdateCount());
                    }
                }
            }catch(SQLException e){
                logger.severe(e.getMessage());
                System.exit(0);
            }
        }
    }
}


