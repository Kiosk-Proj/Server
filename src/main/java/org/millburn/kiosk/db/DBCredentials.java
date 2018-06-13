package org.millburn.kiosk.db;

public class DBCredentials{
    private String username;
    private String pw;

    public DBCredentials(String username, String pw){
        this.username = username;
        this.pw = pw;
    }

    public String getUsername(){
        return username;
    }

    public String getSinglePassword(){
        if(pw.equals("ACCESSED")) throw new SecurityException("Multiple accesses on unencrypted password");

        String temp = pw;
        pw = "ACCESSED";
        return temp;
    }
}
