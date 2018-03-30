package com.wknight.safe.shield.res.arsc;

public class ResStringPoolHeader {

    public ResChunkHeader header;

    public int stringCount;

    public int styleCount;

    public enum Flags {
        SORTED_FLAG(1), UTF8_FLAG(1 << 8);

        private int value;

        Flags(int value) {
            this.value = value;
        }
    }

    public int flags;

    public int stringsStart;

    public int stylesStart;

    public ResStringPoolHeader() {
        header = new ResChunkHeader();
    }
}
