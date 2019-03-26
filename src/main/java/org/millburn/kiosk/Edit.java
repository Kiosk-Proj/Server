package org.millburn.kiosk;

import org.millburn.kiosk.db.SQLFuture;
import org.millburn.kiosk.db.SQLResult;

import java.sql.SQLException;

public class Edit {
    long editId;
    long id;
    String field;
    String value;

    public Edit() {
    }

    public Edit(SQLResult.Row row) {
        try {
            editId = row.getLong("editID");
            id = row.getInt("studentID");
            field = row.getString("field");
            value = row.getString("value");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public long getEditId() {
        return editId;
    }

    public void setEditId(long editId) {
        this.editId = editId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SQLFuture<SQLResult> upload(){
        return Server.getCurrent().getDatabase().query("INSERT INTO `kiosk`.`editlog`(`studentID`,`field`,`value`) " +
                "VALUES(\"" + id + "\",\"" + field + "\",\"" + value + "\")");
    }
}
