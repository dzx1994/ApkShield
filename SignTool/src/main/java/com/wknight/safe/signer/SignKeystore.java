package com.wknight.safe.signer;

import java.util.ArrayList;

public class SignKeystore {
    public static String APK_PATH = "";
    public static String KEYSTORE_PATH = "";
    public static String KEYSTORE_PWD = "";
    public static String KEY_ALIAS = "";
    public static String KEY_PWD = "";
    public static String DESTINATION = "";
    public static String INIFILENAME = "";
    public static ArrayList<String> fileArray;
    public static ArrayList<String> listPath;

    public static void setDestPath(ArrayList<String> destPath)
    {
        listPath = destPath;
    }

    public static void setFileArray(ArrayList<String> file)
    {
        fileArray = file;
    }

    public static void setApkPath(String value)
    {
        APK_PATH = value;
        System.out.println(APK_PATH);
    }

    public static void setKeystorePath(String value)
    {
        KEYSTORE_PATH = value;
        System.out.println(KEYSTORE_PATH);
    }

    public static void setKeystorePassword(String value)
    {
        KEYSTORE_PWD = value;
        System.out.println(KEYSTORE_PWD);
    }

    public static void setKeyAlias(String value)
    {
        KEY_ALIAS = value;
        System.out.println(KEY_ALIAS);
    }

    public static void setKeyPassword(String value)
    {
        KEY_PWD = value;
        System.out.println(KEY_PWD);
    }

    public static void setIniFileName(String value)
    {
        INIFILENAME = value;
        System.out.println(INIFILENAME);
    }
}
