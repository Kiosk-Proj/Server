package org.millburn.kiosk;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Executor{
    public List<ExecutorContainer> containers;
    private static Executor executor;

    public static void initialize(){
        executor = new Executor();
    }

    public static Executor getExecutor(){
        if(executor == null)
            executor = new Executor();
        return executor;
    }

    private Executor(){
        containers = new ArrayList<>();
        var thread = new Thread(() -> {
            while(true){
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                update(10);
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public void update(int time){
        var tlist = containers.stream()
                .peek(c -> c.time -= time)
                .filter(ExecutorContainer::isComplete)
                .peek(ExecutorContainer::execute)
                .collect(Collectors.toList());

        containers.removeAll(tlist);
    }

    public static Sleeper at(Instant instant, Runnable exec){
        return in(Instant.now().until(instant, ChronoUnit.MILLIS), exec);
    }

    public static Sleeper in(long millis, Runnable exec){
        return getExecutor().inInternal(millis, exec);
    }

    private Sleeper inInternal(long millis, Runnable exec){
        if(millis <= 0){
            exec.run();
            return new UselessSleeper();
        }

        var container = new ExecutorContainer(exec, millis);
        containers.add(container);
        return container.sleeper;
    }

    private class ExecutorContainer{
        private Runnable runnable;
        private Sleeper sleeper;
        private long time;

        private ExecutorContainer(Runnable runnable, long time){
            this.runnable = runnable;
            this.sleeper = new Sleeper();
            this.time = time;
        }

        private boolean isComplete(){
            return time < 0;
        }

        private void execute(){
            runnable.run();
            sleeper.awaken();
        }
    }

    public class Sleeper{
        private final Object lock = new Object();

        private Sleeper(){}

        private void awaken(){
            synchronized(lock){
                lock.notifyAll();
            }
        }

        public void waitUntilComplete(){
            synchronized(lock){
                try{
                    lock.wait();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public class UselessSleeper extends Sleeper{
        @Override
        public void waitUntilComplete(){}
    }
}
