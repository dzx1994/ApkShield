package com.wknight.safe.shield.util;

import pxb.android.axml.AXMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AxmlUtil {
    /**
     *
     * @param filename
     * @return debuggable = 0x1 allowBackup = 0x2 name = 0x4
     * @throws IOException
     */
    public static int hasAllowBackupAndDebuggableAndName(String filename) throws IOException {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] xml = new byte[fis.available()];
        fis.read(xml);
        fis.close();

        int count = 0;

        AXMLParser parser = new AXMLParser(xml);

        out:
        while (true) {
            int event = parser.next();
            switch (event) {
                case AXMLParser.START_FILE:
                    break;
                case AXMLParser.END_FILE:
                    break out;
                case AXMLParser.START_TAG:
                    String tagName = parser.getName();
                    if ("application".equals(tagName)) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attr = parser.getAttrName(i);
                            if (attr.contains("debuggable")) {
                                count |= 0x1;
                            }
                            if (attr.contains("allowBackup")) {
                                count |= 0x2;
                            }
                            if (attr.contains("name")) {
                                count |= 0x4;
                            }
                        }
                    }
                case AXMLParser.END_TAG:
                    break;
            }
        }

        return count;
    }
}
