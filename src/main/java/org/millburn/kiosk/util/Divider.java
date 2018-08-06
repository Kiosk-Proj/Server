package org.millburn.kiosk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Divider<T extends Comparable, U> {
    Map<T, List<U>> map = new HashMap<>();

    public void add(T t, U u){
        if(map.containsKey(t))
            map.get(t).add(u);
        else {
            map.put(t, new ArrayList<>());
            map.get(t).add(u);
        }
    }

    public Divider addAll(Divider<T,U> div){
        var map2 = div.map;

        for(var key : map2.keySet()){
            if(map.containsKey(key))
                map.get(key).addAll(map2.get(key));
            else
                map.put(key, map2.get(key));
        }

        return this;
    }

    public Map<T,List<U>> getMap(){
        return map;
    }
}
