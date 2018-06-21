package org.millburn.kiosk.util;

import java.io.*;

public class SerializeUtil{
    public static byte[] readBytes(InputStream in) throws IOException{
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }

    public static void sendBytes(OutputStream out, byte[] myByteArray) throws IOException {
        sendBytes(out, myByteArray, 0, myByteArray.length);
    }

    public static void sendBytes(OutputStream out, byte[] myByteArray, int start, int len) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);

        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }
}
