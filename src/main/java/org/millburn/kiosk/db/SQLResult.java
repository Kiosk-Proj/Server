package org.millburn.kiosk.db;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SQLResult implements Iterable<SQLResult.Row>{
    private ResultSet set;
    private List<Row> rows;

    public SQLResult(ResultSet set) throws SQLException{
        this.set = set;
        var trows = new ArrayList<Row>();
        int i = 0;
        System.out.println(set.getFetchSize());
        while(!set.isLast()){
            System.out.println(set.getRow());
            trows.add(new Row(i));
            set.next();
            i++;

        }

        rows = List.copyOf(trows);
    }

    public List<Row> getRows(){
        return rows;
    }

    @Override
    public Iterator<Row> iterator(){
        return rows.listIterator();
    }

    @Override
    public void forEach(Consumer action){
        rows.forEach(action);
    }

    @Override
    public Spliterator<Row> spliterator(){
        return rows.spliterator();
    }

    public class Row{
        private int id;

        public Row(int id){
            this.id = id;
        }

        public String getString(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getString(columnIndex + 1);
        }

        public boolean getBoolean(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getBoolean(columnIndex + 1);
        }

        public byte getByte(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getByte(columnIndex + 1);
        }

        public short getShort(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getShort(columnIndex + 1);
        }

        public int getInt(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getInt(columnIndex + 1);
        }

        public long getLong(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getLong(columnIndex + 1);
        }

        public float getFloat(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getFloat(columnIndex + 1);
        }

        public double getDouble(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getDouble(columnIndex + 1);
        }

        public byte[] getBytes(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getBytes(columnIndex + 1);
        }

        public Date getDate(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getDate(columnIndex + 1);
        }

        public Time getTime(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getTime(columnIndex + 1);
        }

        public Timestamp getTimestamp(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getTimestamp(columnIndex + 1);
        }

        public InputStream getAsciiStream(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getAsciiStream(columnIndex + 1);
        }

        public InputStream getBinaryStream(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getBinaryStream(columnIndex + 1);
        }

        public String getString(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getString(columnLabel);
        }

        public boolean getBoolean(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getBoolean(columnLabel);
        }

        public byte getByte(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getByte(columnLabel);
        }

        public short getShort(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getShort(columnLabel);
        }

        public int getInt(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getInt(columnLabel);
        }

        public long getLong(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getLong(columnLabel);
        }

        public float getFloat(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getFloat(columnLabel);
        }

        public double getDouble(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getDouble(columnLabel);
        }

        public byte[] getBytes(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getBytes(columnLabel);
        }

        public Date getDate(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getDate(columnLabel);
        }

        public Time getTime(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getTime(columnLabel);
        }

        public Timestamp getTimestamp(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getTimestamp(columnLabel);
        }

        public Object getObject(int columnIndex) throws SQLException{
            set.absolute(id + 1);
            return set.getObject(columnIndex + 1);
        }

        public Object getObject(String columnLabel) throws SQLException{
            set.absolute(id + 1);
            return set.getObject(columnLabel);
        }

        public int findColumn(String columnLabel) throws SQLException{
            return set.findColumn(columnLabel);
        }
    }
}
