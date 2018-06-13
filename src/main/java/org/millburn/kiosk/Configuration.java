package org.millburn.kiosk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Configuration{
    private static final Map<String,ConfigFile> settings = new HashMap<>();

    public static void load(File configfile) throws IOException{
        var properties = new Properties();

        properties.load(new FileInputStream(configfile));

        var propertyset = properties.entrySet();
        var datamap = new HashMap<String, String>();

        for(var entry : propertyset){
            datamap.put((String)entry.getKey(), (String)entry.getValue());
        }

        ConfigFile file = new ConfigFile(configfile.getName(), datamap);
        settings.put(configfile.getName(), file);
    }

    public static ConfigFile getConfigFile(String name){
        return settings.get(name);
    }

    public static String get(String key){
        return getEntry(key).getValue();
    }

    private static Map.Entry<String, String> getEntry(String key){
        return settings.values().stream()
                //.filter(s -> s.name.equals(key.substring(0, key.indexOf('.'))))
                .flatMap(s -> s.getAllSettings().entrySet().stream())
                .filter(set -> set.getKey().equals(key))
                .findFirst()
                .get();
    }

    public static boolean set(String key, String val){
        var count = settings.values().stream()
                .filter(maps -> maps.getAllSettings().containsKey(key))
                .peek(maps -> maps.getAllSettings().replace(key, val))
                .count();

        return count > 0;
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

    public static class ConfigFile {
        private String name;
        private HashMap<String, String> contents;

        public ConfigFile(String name, HashMap<String, String> contents){
            this.name = name;
            this.contents = contents;
        }

        public String getConfig(String name){
            return contents.get(name);
        }

        public Map<String,String> getAllSettings(){
            Map copy = new HashMap<String, String>();
            for(String id : contents.keySet()){
                copy.put(id, contents.get(id));
            }
            return copy;
        }
    }
}
