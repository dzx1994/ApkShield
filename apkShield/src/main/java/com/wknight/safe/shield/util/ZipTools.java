package com.wknight.safe.shield.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {
    private static final int BUFFER = 8192;
    /**
     * @param srcZipFile 需解压的文件
     * @param targetPath 解压的目录路径
     * @return 如果解压成功返回解压目录
     */
    public static String unzipApk(File srcZipFile, String targetPath) {
        String basicName = srcZipFile.getName().replace(".apk","");
        String outPath = targetPath + basicName + File.separator;
        File dir = new File(outPath);
        if (dir.exists()){
            ShieldUtil.deleteDir(dir);
            dir.mkdir();
        }else {
            dir.mkdir();
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcZipFile));
            ZipInputStream zis = new ZipInputStream(bis);

            BufferedOutputStream bos;

            ZipEntry entry;
            while ((entry=zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File(outPath + entryName);

                if (entry.isDirectory()){
                    if (!entryFile.exists()){
                        entryFile.mkdirs();
                        continue;
                    }
                } else if (!entryFile.getParentFile().exists()){
                    entryFile.getParentFile().mkdirs();
                }

                FileOutputStream entryOutputStream = new FileOutputStream(entryFile);
                bos = new BufferedOutputStream(entryOutputStream);
                int b = 0;
                while ((b = zis.read()) != -1) {
                    bos.write(b);
                }
                bos.flush();
                bos.close();
            }
            zis.close();
        } catch (IOException e) {
            outPath = null;
            e.printStackTrace();
        }
        return outPath;
    }

    /**
     * zip压缩功能.
     * 压缩baseDir(文件夹目录)下所有文件，包括子目录
     * @param baseDir 需要压缩的目录路径
     * @param fileName 输出文件的文件名路径
     * @throws Exception
     */
    public static void zipFile(String baseDir,String fileName) throws IOException {
        List fileList=getSubFiles(new File(baseDir));
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
        ZipOutputStream zos=new ZipOutputStream(bos);
        ZipEntry ze=null;
        byte[] buf=new byte[1024];
        int readLen=0;
        for(int i = 0; i <fileList.size(); i++) {
            File f=(File)fileList.get(i);
            ze=new ZipEntry(getAbsFileName(baseDir, f));
            if (!f.getName().endsWith(".dex") &&
                    !f.getName().endsWith(".xml") &&
                    !f.getName().endsWith(".so") &&
                    !f.getName().endsWith(".arsc") &&
                    !f.getName().endsWith(".crt") &&
                    !f.getName().endsWith(".pem")){
                final byte[] fileContents = readContents(f);
                ze.setMethod(ZipEntry.STORED);
                final CRC32 checksumCalculator = new CRC32();
                checksumCalculator.update(fileContents);
                ze.setCrc(checksumCalculator.getValue());
            }
            ze.setSize(f.length());
            ze.setTime(f.lastModified());
            zos.putNextEntry(ze);
            InputStream is=new BufferedInputStream(new FileInputStream(f));
            while ((readLen=is.read(buf, 0, 1024))!=-1) {
                zos.write(buf, 0, readLen);
            }
            is.close();
        }
        zos.close();
        bos.close();
        fos.close();
    }

    private static byte[] readContents(final File file) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int bufferSize = 4096;
        try {
            final FileInputStream in = new FileInputStream(file);
            final BufferedInputStream bIn = new BufferedInputStream(in);
            int length;
            byte[] buffer = new byte[bufferSize];
            byte[] bufferCopy;
            while ((length = bIn.read(buffer, 0, bufferSize)) != -1) {
                bufferCopy = new byte[length];
                System.arraycopy(buffer, 0, bufferCopy, 0, length);
                output.write(bufferCopy);
            }
            bIn.close();
        } finally {
            output.close();
        }
        return output.toByteArray();
    }

    /**
     * 给定根目录，返回另一个文件名的相对路径，用于zip文件中的路径.
     * @param baseDir java.lang.String 根目录
     * @param realFileName java.io.File 实际的文件名
     * @return 相对文件名
     */
    private static String getAbsFileName(String baseDir, File realFileName){
        File real=realFileName;
        File base=new File(baseDir);
        String ret=real.getName();
        while (true) {
            real=real.getParentFile();
            if(real==null)
                break;
            if(real.equals(base))
                break;
            else
                ret=real.getName()+"/"+ret;
        }
        return ret;
    }

    /**
     * 取得指定目录下的所有文件列表，包括子目录.
     * @param baseDir File 指定的目录
     * @return 包含java.io.File的List
     */
    private static List getSubFiles(File baseDir){
        List ret=new ArrayList();
        File[] tmp=baseDir.listFiles();
        for (int i = 0; i <tmp.length; i++) {
            if(tmp[i].isFile())
                ret.add(tmp[i]);
            if(tmp[i].isDirectory())
                ret.addAll(getSubFiles(tmp[i]));
        }
        return ret;
    }

}
