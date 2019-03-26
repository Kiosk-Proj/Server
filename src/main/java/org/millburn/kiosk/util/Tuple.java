package org.millburn.kiosk.util;

public class Tuple<T, T1>{
    public T t;
    public T1 t1;

    public Tuple(){}

    public Tuple(T t, T1 t1){
        this.t = t;
        this.t1 = t1;
    }

    public static <T1,T2> Tuple<T1,T2> of(T1 t1, T2 t2){
        return new Tuple<>(t1,t2);
    }
}
