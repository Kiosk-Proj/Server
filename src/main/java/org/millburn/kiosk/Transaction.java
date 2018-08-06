package org.millburn.kiosk;

import java.time.Instant;

public class Transaction{
    public static long currentId = 0;

    private final long transactionId;
    private final int userId;
    private final String name;
    private final int kiosk;

    private boolean validated;

    private State state;

    public Transaction(int userId, int kiosk, String name){
        this.transactionId = currentId++;
        this.userId = userId;
        this.name = name;
        this.kiosk = kiosk;
        this.validated = true;
    }

    public long getTransactionId(){
        return transactionId;
    }

    public int getUserId(){
        return userId;
    }

    public String getName(){
        return name;
    }

    public State getState(){
        return state;
    }

    public boolean isValidated(){
        return validated;
    }

    public int getKiosk() {
        return kiosk;
    }

    public void setValidated(boolean validated){
        this.validated = validated;
    }

    public void setState(State state){
        this.state = state;
    }

    public enum State{
        LOGIN, EXISTS, CONFIRMED, NO_USER_EXISTS, INCORRECT_USER;
    }
}
