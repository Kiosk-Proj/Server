/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.millburn.kiosk.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Javier
 */
public class GGInputStream extends InputStream{
    private InputStream in;
    
    public GGInputStream(InputStream bais){
        this.in = bais;
    }

    public GGInputStream(byte[] data){
        this(ByteBuffer.wrap(data));
    }

    public GGInputStream(ByteBuffer buffer){
        if(buffer.hasArray()){
            this.in = new ByteArrayInputStream(buffer.array());
        }else{
            byte[] array = new byte[buffer.limit()];
            for(int i = 0; i < array.length; i++){
                array[i] = buffer.get();
            }
            this.in = new ByteArrayInputStream(array);
        }
    }

    public short readShort() throws IOException{
        ByteBuffer b = ByteBuffer.allocate(Short.BYTES).put(readByteArray(Short.BYTES));
        b.flip();
        return b.getShort();
    }

    public int readInt() throws IOException{
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES).put(readByteArray(Integer.BYTES));
        b.flip();
        return b.getInt();
    }

    public long readLong() throws IOException{
        ByteBuffer b = ByteBuffer.allocate(Long.BYTES).put(readByteArray(Long.BYTES));
        b.flip();
        return b.getLong();
    }
    
    public float readFloat() throws IOException{
        ByteBuffer b = ByteBuffer.allocate(Float.BYTES).put(readByteArray(Float.BYTES));
        b.flip();
        return b.getFloat();
    }
    public double readDouble() throws IOException{
        ByteBuffer b = ByteBuffer.allocate(Double.BYTES).put(readByteArray(Double.BYTES));
        b.flip();
        return b.getDouble();
    }
    
    public boolean readBoolean() throws IOException{
        int b = readInt();
        return b == 1;
    }
    
    public byte[] readByteArray(int size) throws IOException{
        byte[] b = new byte[size];
        in.read(b);
        return b;
    }
    
    public byte readByte() throws IOException{
        return (byte) read();
    }
    
    public char readChar() throws IOException{
        ByteBuffer b = ByteBuffer.allocate(Character.BYTES).put(readByteArray(Character.BYTES));
        b.flip();
        return (char)b.getShort();
    }
    
    public String readString() throws IOException{
        StringBuilder s = new StringBuilder();
        int len = readInt();
        for(int i = 0; i < len; i++){
            s.append(readChar());
        }
        return s.toString();
    }
    
    public FloatBuffer readFloatBuffer() throws IOException{
        int len = readInt();
        FloatBuffer fb = FloatBuffer.allocate(len);
        for(int i = 0; i < len; i++){
            fb.put(readFloat());
        }
        fb.flip();
        return fb;
    }
    
    public IntBuffer readIntBuffer() throws IOException{
        int len = readInt();
        IntBuffer ib = IntBuffer.allocate(len);
        for(int i = 0; i < len; i++){
            ib.put(readInt());
        }
        ib.flip();
        return ib;
    }
    
    public ByteBuffer readByteBuffer() throws IOException{
        int len = readInt();
        ByteBuffer bb = ByteBuffer.allocate(len);
        for(int i = 0; i < len; i++){
            bb.put(readByte());
        }
        bb.flip();
        return bb;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }
}
