package org.millburn.kiosk.db.impl;

import org.millburn.kiosk.db.Database;
import org.millburn.kiosk.db.SQLFuture;
import org.millburn.kiosk.db.SQLResult;

import java.sql.ResultSet;

public class TestDatabase implements Database{
    private ProductionDatabase db;

    public TestDatabase(String ip, String user, String pw){
        this.db = new ProductionDatabase(ip, user, pw);
    }

    @Override
    public int updateQuery(String query){
        return db.updateQuery(query);
    }

    @Override
    public SQLResult requestQuery(String query){
        return db.requestQuery(query);
    }

    @Override
    public SQLFuture query(String query){
        return db.query(query);
    }

    @Override
    public void disconnect(){
        db.disconnect();
    }

}
