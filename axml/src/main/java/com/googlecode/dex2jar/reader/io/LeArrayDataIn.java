package com.googlecode.dex2jar.reader.io;


import com.googlecode.dex2jar.reader.io.ArrayDataIn;
import com.googlecode.dex2jar.reader.io.DataIn;

public class LeArrayDataIn extends ArrayDataIn implements DataIn {
    public LeArrayDataIn(byte[] data) {
        super(data);
    }

    public int readUShortx() {
        return this.readUByte() | this.readUByte() << 8;
    }

    public int readUIntx() {
        return this.readUByte() | this.readUByte() << 8 | this.readUByte() << 16 | this.readUByte() << 24;
    }
}
