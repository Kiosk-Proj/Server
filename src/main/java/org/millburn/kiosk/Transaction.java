package org.millburn.kiosk;

import java.time.Instant;

public class Transaction{
    private static long currentId = 0;

    private final long transactionId;
    private final int userId;
    private final Instant receptionTime;

    private State state;

    public Transaction(int userid){
        transactionId = currentId++;
        this.userId = userid;
        receptionTime = Instant.now();
    }

    public long getTransactionId(){
        return transactionId;
    }

    public int getUserId(){
        return userId;
    }

    public State getState(){
        return state;
    }

    public void state(State state){
        this.state = state;
    }

    public enum State{
        LOGIN, EXISTS, CONFIRMED, NO_USER_EXISTS, INCORRECT_USER;
    }
}
