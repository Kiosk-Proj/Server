package org.millburn.kiosk;

import org.millburn.kiosk.db.SQLResult;

import java.sql.SQLException;

public class Student {

    public static Student nonexistent;

    static{
        nonexistent = new Student(" ,Nonexistent,error.jpg,-1,-1,false");
    }

    String name;
    String path;
    String grade;
    String id;
    boolean seniorPriv;
    boolean isIn;

    public Student(String val) {
        String[] vals = val.split(",");

        var lastName = vals[0];
        var firstName = vals[1];
        name = firstName + " " + lastName;
        path = vals[2];
        grade = vals[3];
        id = vals[4];
        seniorPriv = Boolean.parseBoolean(vals[5]);
        isIn = true;
    }

    public static Student getNonexistent(int id){
        return new Student(" ,Nonexistent,error.jpg,-1,"+id+",false");
    }

    public Student(SQLResult.Row row) {
        try {
            name = row.getString("personname");
            path = row.getString("image");
            grade = row.getString("grade");
            id = row.getString("ID");
            seniorPriv = row.getBoolean("seniorPriv");
            isIn = row.getBoolean("isIn");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void upload() {
        Server.getCurrent().getDatabase().query("INSERT INTO `kiosk`.`allstudents`(`ID`,`personname`,`image`,`grade`,`seniorPriv`,`isIn`) " +
                "VALUES(\"" + id + "\",\"" + name + "\",\"" + path + "\",\"" + grade + "\",\"" + (seniorPriv ? 1 : 0) + "\",\"" + 1 + "\")" +
                "  ON DUPLICATE KEY UPDATE personname=\"" + name + "\", image=\"" + path + "\", grade=\"" + grade + "\", seniorPriv=\"" + (seniorPriv ? 1 : 0) + "\";");
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getGrade() {
        return grade;
    }

    public String getId() {
        return id;
    }

    public boolean isSeniorPriv() {
        return seniorPriv;
    }

    public boolean isIn() {
        return isIn;
    }
}