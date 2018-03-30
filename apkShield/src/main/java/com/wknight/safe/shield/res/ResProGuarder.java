package com.wknight.safe.shield.res;

import com.wknight.safe.shield.res.arsc.Resources;
import com.wknight.safe.shield.res.io.LEDataInputStream;
import com.wknight.safe.shield.res.io.LEDataOutputStream;
import com.wknight.safe.shield.util.ShieldUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ResProGuarder {
    private String inPath;
    private String outPath;
    private File srcApkPath;
    private String outDir;
    private Map<String, String> changeMap;

    public ResProGuarder(String operateDir, File srcApk){
        inPath = operateDir + File.separator + "resources.arsc";
        outPath = operateDir + File.separator + "resources.arsc_new";
        srcApkPath = srcApk;
        outDir = operateDir;
        changeMap = new HashMap<String, String>();
    }

    public void handle_utf8() throws Exception {
        InputStream is = new FileInputStream(inPath);
        OutputStream os = new FileOutputStream(outPath);

        ZipFile zipFile = new ZipFile(srcApkPath);
        FileInputStream apkIn = new FileInputStream(srcApkPath);
        InputStream zipIn = new BufferedInputStream(apkIn);
        ZipInputStream zis = new ZipInputStream(zipIn);
        ZipEntry ze;
        Map<String, ZipEntry> zipFileNames = new HashMap<String, ZipEntry>();
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.isDirectory()) {
                //ignore
            } else {
                zipFileNames.put(ze.getName(), ze);
            }
        }
        zis.closeEntry();

        DataInput dip = new LEDataInputStream(is);
        DataOutput dop = new LEDataOutputStream(os);

        Resources resources = new Resources();

        resources.resTableHeader.header.type = dip.readShort();
        resources.resTableHeader.header.headerSize = dip.readShort();
        resources.resTableHeader.header.size = dip.readInt();
        resources.resTableHeader.packageCount = dip.readInt();

        dop.writeShort(resources.resTableHeader.header.type);
        dop.writeShort(resources.resTableHeader.header.headerSize);
        dop.writeInt(resources.resTableHeader.header.size);
        dop.writeInt(resources.resTableHeader.packageCount);

        resources.resStringPoolHeader.header.type = dip.readShort();
        resources.resStringPoolHeader.header.headerSize = dip.readShort();
        resources.resStringPoolHeader.header.size = dip.readInt();       //块大小
        resources.resStringPoolHeader.stringCount = dip.readInt();
        resources.resStringPoolHeader.styleCount = dip.readInt();
        resources.resStringPoolHeader.flags = dip.readInt();
        resources.resStringPoolHeader.stringsStart = dip.readInt();
        resources.resStringPoolHeader.stylesStart = dip.readInt();

        dop.writeShort(resources.resStringPoolHeader.header.type);
        dop.writeShort(resources.resStringPoolHeader.header.headerSize);
        dop.writeInt(resources.resStringPoolHeader.header.size);
        dop.writeInt(resources.resStringPoolHeader.stringCount);
        dop.writeInt(resources.resStringPoolHeader.styleCount);
        dop.writeInt(resources.resStringPoolHeader.flags);
        dop.writeInt(resources.resStringPoolHeader.stringsStart);
        dop.writeInt(resources.resStringPoolHeader.stylesStart);

        byte[] buf = new byte[4];
        int sum = 0;
        while (sum < resources.resStringPoolHeader.stringsStart - resources.resStringPoolHeader.header.headerSize) {//resStringPoolHeader.stringsStart-resStringPoolHeader.header.headerSize（整头大小=24=1C）=字符偏移数组 + style偏移数组
            sum += 4;
            is.read(buf);
            os.write(buf);
        }

        File dir = new File(outDir);
        String[] names = new String[resources.resStringPoolHeader.stringCount];//字符串数

        for (int i = 0; i < resources.resStringPoolHeader.stringCount; i++) {  //字符串表格式=长度+实际字符+（0000结尾）
//            if (i == 1236){
//                System.out.println(i);
//            }
            /**
             * 1.hbyte & 0x80 is >= 0x80
             * 2.len1 = (hbyte & 0x7F)<<8
             * 3.len = len1 | lbyte
             */
            int count;
            byte hbyte = dip.readByte();
            dop.writeByte(hbyte);
            if ((hbyte & 0x80) != 0) {
                hbyte = dip.readByte();
                dop.writeByte(hbyte);
                hbyte = dip.readByte();
                dop.writeByte(hbyte);
            } else {
                hbyte = dip.readByte();
                dop.writeByte(hbyte);
            }

            if ((hbyte & 0x80) != 0) {
                int len1 = (hbyte & 0x7f) << 8;
                byte b = dip.readByte();
                count = len1 | (b & 0x000000FF);
                dop.writeByte(b);
            } else {
                count = hbyte;
            }

//            System.out.println(i);
            byte[] sb = new byte[count];
            for (int j = 0; j < count; j++) {
                byte b = dip.readByte();
                sb[j] = b;
            }
            names[i] = new String(sb, "UTF-8");

            if (zipFileNames.containsKey(names[i]) && names[i].startsWith("res")) {

                ZipEntry zipEntry = zipFileNames.get(names[i]);
                if (zipEntry == null) {
                    throw new RuntimeException("zip entry is null");
                }

                int sIndex = names[i].lastIndexOf('/') + 1;
                int eIndex = names[i].lastIndexOf('.');
                String baseName;
                String extName;
                if (eIndex != -1) {
                    baseName = names[i].substring(sIndex, eIndex);
                    extName = names[i].substring(eIndex);
                } else {
                    baseName = names[i].substring(sIndex);
                    extName = "";
                }

                char[] newNameChar = new char[baseName.length()];
                getNewName(newNameChar);
                String newName = names[i].substring(0, sIndex) + new String(newNameChar) + extName;

                while (zipFileNames.containsKey(newName)) {
                    getNewName(newNameChar);
                    newName = names[i].substring(0, sIndex) + new String(newNameChar) + extName;
                }

                changeMap.put(names[i], newName);

                zipFileNames.remove(names[i]);
                zipFileNames.put(newName, null);

//                String newName = names[i];

                for (int j = 0; j < count; j++) {
                    byte[] back = newName.getBytes("UTF-8");
                    dop.writeByte(back[j]);
                }

                File newFile = new File(dir.getAbsolutePath() + File.separator + newName);
                if (!newFile.exists()){
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                }
                BufferedOutputStream fos = new BufferedOutputStream(
                        new FileOutputStream(newFile)
                );

                long size = zipEntry.getSize();
                if (size > 0) {
                    BufferedInputStream br = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                    byte[] tmp = new byte[1024];
                    int len;
                    while ((len = br.read(tmp)) != -1) {
                        fos.write(tmp, 0, len);
                    }
                    br.close();
                    fos.close();
                }

                names[i] = newName;
                zis.closeEntry();
            } else {
                byte[] back = names[i].getBytes("UTF-8");
                for (int j = 0; j < count; j++) {
                    dop.writeByte(back[j]);
                }
            }

//            System.out.println(names[i]);

            byte b = dip.readByte();
            dop.writeByte(b);

        }

        dop.writeByte(0);
        dip.skipBytes(1);

        resources.strings = names;

        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

        is.close();
        os.close();

        zipIn.close();
        apkIn.close();
        zipFile.close();

        File oldArsc = new File(inPath);
        File newArsc = new File(outPath);
        if (oldArsc.exists())
            oldArsc.delete();
        newArsc.renameTo(oldArsc);
    }

    static final char[] CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_'};

    private static void getNewName(char[] newName) {
        int count = newName.length;
        int bound = CHARS.length;
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            newName[i] = CHARS[random.nextInt(bound)];
        }
    }

    public Map<String, String> getChangeMap(){
        return changeMap;
    }
}
