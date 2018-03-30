package com.wknight.safe.shield.util;

import java.io.File;
import java.util.Properties;

public class PackUtil {

    static {
        File lib;
        Properties props = System.getProperties();
        String osName = props.getProperty("os.name");
        String ars = props.getProperty("os.arch");
        if (osName.equals("Linux")){
            if (ars.contains("64")){
                lib = new File("bin" + File.separator + "shield64.so");
            }else {
                lib = new File("bin" + File.separator + "shield.so");
            }
        }else {
            if (ars.contains("64")){
                lib = new File("bin" + File.separator + "shield64.dll");
            }else {
                lib = new File("bin" + File.separator + "shield.dll");
            }
        }
        System.load(lib.getAbsolutePath());
    }

    public static native void equip_shield(int count, String[] args);

    public static native void rc4Crypt(int count, String[] args);

    public static native int compressDex(String filename, String output);

    public static native int uncompressDex(String filename, String output);
}
