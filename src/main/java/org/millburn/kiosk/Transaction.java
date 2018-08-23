package org.millburn.kiosk;

import java.time.Instant;

public class Transaction{
    public static long currentId = 0;

    private final long transactionId;
    private final int userId;
    private final int kiosk;

    public Transaction(int userId, int kiosk){
        this.transactionId = currentId++;
        this.userId = userId;
        this.kiosk = kiosk;
    }

    public long getTransactionId(){
        return transactionId;
    }

    public int getUserId(){
        return userId;
    }

    public int getKiosk() {
        return kiosk;
    }
}
