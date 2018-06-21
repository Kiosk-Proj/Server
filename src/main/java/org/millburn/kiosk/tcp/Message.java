package org.millburn.kiosk.tcp;

import org.millburn.kiosk.util.GGInputStream;
import org.millburn.kiosk.util.GGOutputStream;
import org.millburn.kiosk.util.SerializeUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.Instant;

import static org.millburn.kiosk.util.SerializeUtil.readBytes;

public class Message{
    public static final int CONNECTION = 0,
                            INPUT = 1,
                            INPUTRESPONSE = 2,
                            NUMCONFIRM = 3,
                            TABLETUPDATE = 4,
                            TABLETCONFIRM = 5,
                            TABLETCHANGE = 6;

    Instant instant;
    int type;
    long transationId;

    ByteBuffer data;

    public static Message read(InputStream instream) throws IOException{
        return new Message(instream);
    }

    private Message(InputStream instream) throws IOException{
        var in = new GGInputStream(SerializeUtil.readBytes(instream));
        instant = Instant.now();
        type = in.readInt();
        transationId = in.readLong();
        data = in.readByteBuffer();
    }

    public Message(int type, long transactionId, byte[] data){
        this.type = type;
        this.transationId = transactionId;
        this.data = ByteBuffer.wrap(data);
    }

    public GGInputStream getDataStream(){
        return new GGInputStream(data);
    }

    public void write(OutputStream outstream) throws IOException{
        var out = new GGOutputStream();
        out.write(type);
        out.write(transationId);
        out.write(data.array().length);
        out.write(data.array());
        SerializeUtil.sendBytes(outstream, ((ByteArrayOutputStream) out.getStream()).toByteArray());
    }
}
