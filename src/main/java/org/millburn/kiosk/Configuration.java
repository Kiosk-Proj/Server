package org.millburn.kiosk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Configuration{
    private static final Map<File,Properties> settings = new HashMap<>();

    public static void load(File configfile) throws IOException{
        var properties = new Properties();

        properties.load(new FileInputStream(configfile));

        settings.put(configfile, properties);
    }


    public static String get(String key){
        return settings.values().stream()
                .filter(set -> set.getProperty(key) != null)
                .map( set -> set.getProperty(key))
                .findFirst()
                .get();
    }

    public static boolean set(String key, String val){
        return settings.entrySet().stream()
                .filter(set -> set.getValue().getProperty(key) != null)
                .peek(set -> {
                    try {
                        set.getValue().setProperty(key, val);
                        set.getValue().store(new FileOutputStream(set.getKey()), "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .findAny()
                .isPresent();
    }

    public static float getFloat(String key){
        var result = get(key);
        try{
            return Float.parseFloat(result);
        }catch (Exception e){
            return 0;
        }
    }

    public static int getInt(String key){
        var result = get(key);
        try{
            return Integer.parseInt(result);
        }catch (Exception e){
            return 0;
        }
    }

    private Configuration() {
    }

}
