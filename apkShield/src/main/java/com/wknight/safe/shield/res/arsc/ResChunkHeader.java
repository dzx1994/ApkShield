package com.wknight.safe.shield.res.arsc;

public class ResChunkHeader {
    public static final short RES_TABLE_TYPE = 0x0002;
    public static final short RES_STRING_POOL_TYPE = 0x0001;
    public static final short RES_TABLE_PACKAGE_TYPE = 0x0200;

    public short type;

    public short headerSize;

    public int size;
}
