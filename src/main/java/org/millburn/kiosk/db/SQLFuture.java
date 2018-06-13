package org.millburn.kiosk.db;

import java.sql.ResultSet;

public class SQLFuture{
    private final Object lock = new Object();

    private SQLResult t;
    private int updateamount;
    private boolean exists;

    public void set(SQLResult set, int updateamount){
        this.t = set;
        this.updateamount = updateamount;
        exists = true;
        synchronized(lock){
            lock.notifyAll();
        }
    }

    public boolean exists(){
        return exists;
    }

    public SQLResult getResults(){
        waitForCompletion();
        return t;
    }

    public int getUpdateAmount(){
        waitForCompletion();
        return updateamount;
    }

    private void waitForCompletion(){
        if(!exists){
            synchronized(lock){
                try{
                    lock.wait();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
