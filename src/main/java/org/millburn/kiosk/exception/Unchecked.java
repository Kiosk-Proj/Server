package org.millburn.kiosk.exception;

public class Unchecked {
    public static Runnable check(CheckedRunnable r){
        return r;
    }
}
