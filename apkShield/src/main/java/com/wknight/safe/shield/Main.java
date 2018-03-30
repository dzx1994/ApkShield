package com.wknight.safe.shield;

import com.wknight.safe.shield.Interface.IWKnight_shieldEngine;
import com.wknight.safe.shield.engine.ApkShieldEngine;
import com.wknight.safe.shield.engine.JarShieldEngine;
import com.wknight.safe.shield.util.ShieldUtil;

import java.io.File;

public class Main {

    private static IWKnight_shieldEngine engine;
//    private static final String inputDirPath = "input/";
    private static String inputPath = null;
    private static final String outputDirPath = "output" + File.separator;
    private static final String tmpDirPath = "tmp" + File.separator;
    private static final int TYPE_APK = 0;
    private static final int TYPE_JAR = 1;

    public static void main(String[] args){
        String parm1 = args[0];

        if (parm1 != null && parm1.equals("help")){
            printHelp();
        }else {
            for (int i=0; i<args.length; i++){
                if (args[i].startsWith("-")){
                    if (args[i].equals("-c")) cleanWorkspace();
                } else {
                    inputPath = args[i];
                }
            }
        }

        File inFile = new File(inputPath);
        startRePackage(inFile);

    }

    private static void printHelp(){
        System.out.println("usage:java -jar inputFile [-option]");
        System.out.println("option:");
        System.out.println("    -c  clean workspace data");
    }

    private static int checkFileType(File file){
        //TODO add check file type
        return TYPE_APK;
    }

    private static void cleanWorkspace(){
        ShieldUtil.deleteDir(new File(tmpDirPath));
        ShieldUtil.deleteDir(new File(outputDirPath));
    }

    private static void startRePackage(File file){
        if (!file.exists()) printException();

        int typeFlag = checkFileType(file);

        switch (typeFlag){
            case TYPE_APK:
                engine = new ApkShieldEngine(file, tmpDirPath, outputDirPath);
                break;
            case TYPE_JAR:
                engine = new JarShieldEngine();
                break;
        }

        engine.startRebuild();
    }

    private static void printException(){
        System.out.println("no such file!!");
    }
}
