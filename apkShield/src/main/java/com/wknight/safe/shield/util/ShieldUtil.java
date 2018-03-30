package com.wknight.safe.shield.util;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class ShieldUtil {
    public static final int TYPE_JPG = 0x79;
    public static final int TYPE_GIF = 0x83;
    public static final int TYPE_PNG = 0x88;
    public static final int TYPE_BMP = 0x93;
    public static final int TYPE_UNKNOWN = 0x1001;

    /**
     * copy file to target directory by using IoStream
     * @param source source file to copy
     * @param dest target file
     * @throws java.io.IOException
     */
    public static void copyFile(String source, String dest) throws IOException {
        File srcFile = new File(source);
        File destFile = new File(dest);
        copyFile(srcFile, destFile);
    }

    public static void copyFile(File source, File dest) throws IOException {

        if (dest.getParentFile().isDirectory() && !dest.getParentFile().exists()){
            dest.getParentFile().mkdirs();
            if (!dest.exists()) dest.createNewFile();
        }

        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            output.flush();
        }finally {
            if (input != null) input.close();
            if (output != null) output.close();
        }
    }

    /**
     * copy all files in source directory to target directory
     * @param srcDir source directory
     * @param destDir target directory
     */
    public static void copyFiles(String srcDir, String destDir) throws IOException{
        File src = new File(srcDir);
        File dest = new File(destDir);

        if (!dest.exists()) dest.mkdirs();

        if (src.isDirectory() && dest.isDirectory()){
            File[] files = src.listFiles();
            for (File file : files){
                if (file.isDirectory()){
                    copyFiles(file.getAbsolutePath(), destDir + file.getName() + File.separator);
                }else {
                    copyFile(file, new File(destDir + file.getName()));
                }
            }
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
//        System.out.println(dir.getAbsolutePath());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
            }
        }

        boolean f = dir.delete();
        // 目录此时为空，可以删除
        return f;
    }

    /*
     * 通过递归得到某一路径下所有的目录及其文件
     */
    public static List<File> getFiles(String filePath) {
        List<File> fileList = new ArrayList<File>();
        File root = new File(filePath);
        if (root.exists()){
//            if (root.isDirectory())
//                System.out.println("true");
        }
        File[] files = root.listFiles();
        if (files == null) return null;
        for (File file : files) {
            if (file.isDirectory()) {
                fileList.addAll(getFiles(file.getPath()));
//                System.out.println("显示" + filePath + "下所有子目录及其文件" + file.getAbsolutePath());
            } else {
                fileList.add(file);
//                System.out.println("显示" + filePath + "下所有子目录" + file.getAbsolutePath());
            }
        }
        return fileList;
    }

    /**
     * byte数组转换成16进制字符串
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 根据文件流判断图片类型
     * @param f
     * @return jpg/png/gif/bmp
     */
    public static int getPicType(File f) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println("file not found, can not check type!");
        }
        //读取文件的前几个字节来判断图片格式
        byte[] b = new byte[4];
        try {
            fis.read(b, 0, b.length);
            String type = bytesToHexString(b).toUpperCase();
            if (type.contains("FFD8FF")) {
                return TYPE_JPG;
            } else if (type.contains("89504E47")) {
                return TYPE_PNG;
            } else if (type.contains("47494638")) {
                return TYPE_GIF;
            } else if (type.contains("424D")) {
                return TYPE_BMP;
            }else{
                return TYPE_UNKNOWN;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                fis.close();
            } catch (IOException e) {
                System.out.println("close file fail!");
            }
        }
        return 0;
    }

    /**
     *
     * @param file
     * @param algorithm 所请求算法的名称  for example: MD5, SHA1, SHA-256, SHA-384, SHA-512 etc.
     * @return
     */
    public static String getFileDigest(File file,String algorithm) {

        if (!file.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;

        try {
            digest = MessageDigest.getInstance(algorithm);
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        byte[] digestByte = digest.digest();
        return BaseFunc.Base64Encode(digestByte);
    }
}
