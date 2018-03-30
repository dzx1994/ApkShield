package com.googlecode.dex2jar.reader.io;


import java.io.IOException;

public interface DataOut {
    void writeBytes(byte[] var1) throws IOException;

    void writeByte(int var1) throws IOException;

    void writeInt(int var1) throws IOException;

    void writeShort(int var1) throws IOException;
}

