package com.wknight.safe.shield.res.arsc;

public class ResTablePackage {
    public ResChunkHeader header;

    public int id;

    public char[] name = new char[128];

    public int typeStrings;

    public int lastPublicType;

    public int keyString;

    public int lastPublicKey;

    public ResTablePackage() {
        header = new ResChunkHeader();
    }
}
