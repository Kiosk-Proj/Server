package org.millburn.kiosk.db;

import org.millburn.kiosk.db.impl.ProductionDatabase;
import org.millburn.kiosk.logging.Logger;
import org.millburn.kiosk.util.Tuple;

public interface Database{
    static Database getDatabase(String url, String name, String password){
        return new ProductionDatabase(url, name, password);
    }

    static void register(String value){
        try{
            Class.forName(value);
        }catch(ClassNotFoundException e){
            Logger logger = new Logger();
            logger.severe("Failed to register driver " + value);
        }
    }

    SQLResult requestQuery(String query);

    SQLFuture<SQLResult> query(String query);

    SQLFuture<SQLResult> requestProcedure(String procname, Tuple<ValueTypes, Object>... args);

    SQLResult runProcedure(String procname, Tuple<ValueTypes, Object>... args);

    void disconnect();

    enum ValueTypes{
        STRING, INT, FLOAT, DOUBLE, BYTE, BOOLEAN, LONG
    }
}
