package com.wknight.safe.shield.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManifestParser {

    private static String findPkgName(List<String> inBr) throws IOException {

        String pkgName = "";
        String manifestTag = "E: manifest";
        String packageTag = "A: package";
        String rootNode = "";

        for (String line : inBr){
            if (line.contains(manifestTag)) {
                rootNode = manifestTag;
            } else if (line.contains(packageTag) && rootNode.contains(manifestTag)) {
                pkgName = line.split("\"")[1];
                break;
            }
        }

        return pkgName;
    }

    private static String findAppName(List<String> inBr) throws IOException {
        String appName = "";
        String applicationTag = "E: application";
        String androidNameTag = "A: android:name";
        String androidNoNameTag = "A: :(0x01010003)";
        String rootNode = "";

        for (String line : inBr){
            //获得命令执行后在控制台的输出信息
            if (line.contains(applicationTag)) {
                rootNode = applicationTag;
            } else if (line.contains("E: ") && rootNode.contains(applicationTag)) {
                appName = "";
                break;
            } else if (line.contains(androidNameTag) && rootNode.contains(applicationTag)) {
                appName = line.split("\"")[1];
                break;
            } else if (line.contains(androidNoNameTag) && rootNode.contains(applicationTag)) {
                appName = line.split("\"")[1];
                break;
            }
        }

        return appName;
    }

    public static Map<String, String> findPkgNameAppName(BufferedReader inBr) throws IOException{
        Map<String, String> map = new HashMap<String, String>();

        String line;
        List<String> stringList = new ArrayList<String>();
        while ((line = inBr.readLine()) != null) {
            stringList.add(line);
        }

        String appName = findAppName(stringList);
        String pkgName = findPkgName(stringList);

        map.put("pkgName", pkgName);
        map.put("appName", appName);

        return map;
    }
}
