package org.millburn.kiosk.db;

import java.sql.ResultSet;

public class SQLFuture<T>{
    private final Object lock = new Object();

    private T t;
    private boolean exists;

    public void set(T val, int updateamount){
        this.t = val;
        exists = true;
        synchronized(lock){
            lock.notifyAll();
        }
    }

    public boolean exists(){
        return exists;
    }

    public T getResults(){
        waitForCompletion();
        return t;
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
