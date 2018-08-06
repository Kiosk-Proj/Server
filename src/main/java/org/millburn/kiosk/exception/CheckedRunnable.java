package org.millburn.kiosk.exception;

import java.io.IOException;

public interface CheckedRunnable extends Runnable{
    @Override
    default void run(){
        try {
            run2();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    void run2() throws Exception;

}
