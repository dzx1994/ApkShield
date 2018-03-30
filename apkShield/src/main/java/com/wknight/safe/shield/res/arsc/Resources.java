package com.wknight.safe.shield.res.arsc;

public class Resources {

    public ResTableHeader resTableHeader;

    public ResStringPoolHeader resStringPoolHeader;

    public int[] stringsOffset;

    public int[] stylesOffset;

    public String[] strings;

    public String[] styles;

    public ResTablePackage resTablePackage;

    public Resources() {
        resTableHeader = new ResTableHeader();
        resStringPoolHeader = new ResStringPoolHeader();
        resTablePackage = new ResTablePackage();
    }
}
