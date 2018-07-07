package org.millburn.kiosk.db.impl;

import org.millburn.kiosk.db.*;
import org.millburn.kiosk.logging.Logger;

import java.sql.ResultSet;

public class ProductionDatabase implements Database{
    Logger logger = new Logger();
    public static final int AMOUNT = 10;
    private String name, user, pw;
    private DatabaseConnectionPool pool;

    public ProductionDatabase(String name, String user, String pw){
        this.name = name;
        this.user = user;
        this.pw = pw;
        logger.info("Connecting to database " + name.substring(name.lastIndexOf("/") + 1) +  " at " + name.substring(0, name.lastIndexOf("/")) + " with username " + user);
        pool = new DatabaseConnectionPool(name, user, pw, AMOUNT);
        pool.run();
    }

    @Override
    public SQLResult requestQuery(String query){
        return pool.query(query).getResults();
    }

    @Override
    public SQLFuture query(String query){
        return pool.query(query);
    }

    @Override
    public SQLProcedureResult runProcedure(String procname, String... args) {
        return pool.execPrepared(procname, args).getResults();
    }


    @Override
    public void disconnect(){
        //todo
    }
}
