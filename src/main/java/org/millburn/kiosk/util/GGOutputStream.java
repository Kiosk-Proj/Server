/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.millburn.kiosk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Javier
 */
public class GGOutputStream extends OutputStream{
    private OutputStream out;
    
    public GGOutputStream(){
        this.out = new ByteArrayOutputStream();
    }
    
    public GGOutputStream(OutputStream baos){
        this.out = baos;
    }

    public void write(long l) throws IOException{
        write(ByteBuffer.allocate(Long.BYTES).putLong(l).array());
    }

    public void write(short i) throws IOException{
        write(ByteBuffer.allocate(Short.BYTES).putShort(i).array());
    }

    @Override
    public void write(int i) throws IOException{
        write(ByteBuffer.allocate(Integer.BYTES).putInt(i).array());
    }
    
    public void write(float f) throws IOException{
        write(ByteBuffer.allocate(Float.BYTES).putFloat(f).array());
    }
    public void write(double f) throws IOException{
        write(ByteBuffer.allocate(Double.BYTES).putDouble(f).array());
    }
    
    public void write(boolean b) throws IOException{
        write(b ? 1 : 0);
    }
    
    @Override
    public void write(byte[] b) throws IOException{
        out.write(b);
    }
    
    public void write(byte b) throws IOException{
        out.write(b);
    }
    
    public void write(char c) throws IOException{
        write(ByteBuffer.allocate(Character.BYTES).putChar(c).array());
    }
    
    public void write(String s) throws IOException{
        write(s.length());
        for(char c : s.toCharArray()){
            write(c);
        }
    }
    
    public void write(FloatBuffer fb) throws IOException{
        fb.rewind();
        write(fb.limit());
        while(fb.hasRemaining())
            write(fb.get());
    }
    
    public void write(IntBuffer ib) throws IOException{
        ib.rewind();
        write(ib.limit());
        while(ib.hasRemaining())
            write(ib.get());
    }

    public OutputStream getStream(){
        return out;
    }

    public byte[] getData(){
        return ((ByteArrayOutputStream)out).toByteArray();
    }
    
    @Override
    public void flush() throws IOException{
        out.flush();
    }
    
    @Override
    public void close() throws IOException{
        out.close();
    }
}
