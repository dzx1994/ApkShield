package com.wknight.safe.signer;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipUtil {
    private static final int BUFFER = 1024;
    private static final String BASE_DIR = "";
    private static final String PATH = "/";

    public boolean unZip(String fileName)
            throws Exception
    {
        try
        {
            ZipFile zipFile = new ZipFile(fileName);


            Enumeration emu = zipFile.entries();
            while (emu.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry)emu.nextElement();
                if (entry.getName().startsWith("META-INF"))
                {
                    zipFile.close();
                    return true;
                }
            }
            zipFile.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void unZip(String fileName, String filePath)
            throws Exception
    {
        ZipFile zipFile = new ZipFile(fileName);


        Enumeration emu = zipFile.entries();
        while (emu.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry)emu.nextElement();
            if (entry.isDirectory())
            {
                new File(filePath + "/" + entry.getName()).mkdirs();
            }
            else
            {
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

                File file = new File(filePath + "/" + entry.getName());
                File parent = file.getParentFile();
                if ((parent != null) && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = bis.read(buf, 0, 1024)) != -1) {
                    fos.write(buf, 0, len);
                }
                bos.flush();
                bos.close();
                bis.close();
            }
        }
        zipFile.close();
    }

    public void compress(String srcFile, String destPath)
            throws Exception
    {
        compress(new File(srcFile), new File(destPath));
    }

    public void compress(File srcFile, File destFile)
            throws Exception
    {
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
                destFile), new CRC32());

        ZipOutputStream zos = new ZipOutputStream(cos);
        compress(srcFile, zos, "");

        zos.flush();
        zos.close();
    }

    private void compress(File srcFile, ZipOutputStream zos, String basePath)
            throws Exception
    {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    private void compressDir(File dir, ZipOutputStream zos, String basePath)
            throws Exception
    {
        File[] files = dir.listFiles();
        if (files.length < 1)
        {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + "/");

            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        String dirName = "";
        String path = "";
        for (File file : files)
        {
            if ((basePath != null) && (!"".equals(basePath))) {
                dirName = dir.getName();
            }
            path = basePath + dirName + "/";

            compress(file, zos, path);
        }
    }

    private void compressFile(File file, ZipOutputStream zos, String dir)
            throws Exception
    {
        if ("/".equals(dir)) {
            dir = "";
        } else if (dir.startsWith("/")) {
            dir = dir.substring(1, dir.length());
        }
        ZipEntry entry = new ZipEntry(dir + file.getName());
        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        byte[] data = new byte[1024];
        int count;
        while ((count = bis.read(data, 0, 1024)) != -1)
        {
            zos.write(data, 0, count);
        }
        bis.close();

        zos.closeEntry();
    }
}
