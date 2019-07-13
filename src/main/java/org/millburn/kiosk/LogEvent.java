package org.millburn.kiosk;

import org.millburn.kiosk.db.Database;
import org.millburn.kiosk.db.SQLResult;

import java.sql.SQLException;
import java.time.Instant;

public class LogEvent {
    public static int currentId;
    int id;
    long transaction;
    Instant date;
    int kiosk;
    boolean valid;

    public static LogEvent createFake(Student student, int kiosk){
        return new LogEvent(Integer.parseInt(student.id), -1, Instant.now(), kiosk, false);
    }

    public LogEvent(int id, long transaction, Instant date, int kiosk, boolean valid) {
        this.id = id;
        this.transaction = transaction;
        this.date = date;
        this.kiosk = kiosk;
        this.valid = valid;
    }

    public LogEvent(SQLResult.Row row) {
        try {
            this.id = row.getInt("ID");
            this.kiosk = row.getInt("kiosk");
            this.transaction = row.getLong("transaction");
            this.valid = row.getBoolean("valid");
            this.date = row.getTimestamp("timelog").toInstant();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getId() {
        return id;
    }

    public long getTransaction() {
        return transaction;
    }

    public Instant getDate() {
        return date;
    }

    public int getKiosk(){
        return kiosk;
    }

    public boolean isValid() {
        return valid;
    }

    public void upload() {
        Server.getCurrent().getDatabase().query("INSERT INTO `kiosk`.`violations`(id,transaction) VALUES(" + id + "," + transaction + ");");
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "id=" + id +
                ", transaction=" + transaction +
                '}';
    }
}