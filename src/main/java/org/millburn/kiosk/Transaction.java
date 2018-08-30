package org.millburn.kiosk;

import java.time.Instant;

public class Transaction{
    public static long currentId = 0;

    private final long transactionId;
    private final int userId;
    private final int kiosk;
    private final boolean valid;

    public Transaction(int userId, int kiosk, boolean valid){
        this.transactionId = valid ? currentId++ : -1;
        this.userId = userId;
        this.kiosk = kiosk;
        this.valid = valid;
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

    public boolean isValid() {
        return valid;
    }
}
