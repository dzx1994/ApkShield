package com.wknight.safe.shield.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;

public class ShieldDexTool {

    public static void mergeDex(String mainDexPath, List<String> otherPaths, String shieldPath, String outDir) {
        try {
            int dexNumber = otherPaths.size() + 1;
            File unShellDexFile = new File(shieldPath);//解壳用dex
            File mainDexFiel = new File(mainDexPath);
            byte[] unShellDexByte = readFileBytes(unShellDexFile);
            byte[] header = "wks_".getBytes("utf-8");
            byte[] dexNumberByte = intToByte2(dexNumber);
            byte[] mainDexByte = readFileBytes(mainDexFiel);
            byte[] mainDexLenByte = intToByte2(mainDexByte.length);

            int unShellDexByteLen = unShellDexByte.length;//解壳dex长
            int headerLen = header.length;//头长度
            int dexNumberLen = 4;
            int mainDexLenByteLen = 4;
            int mainDexByteLen = mainDexByte.length;

            int totalLen = unShellDexByteLen + headerLen + dexNumberLen + mainDexLenByteLen + mainDexByteLen;

            System.out.println(">>>>" + mainDexFiel.delete());
            System.out.println(">>>>" + unShellDexFile.delete());

            byte[] newDexByte;
            if (dexNumber == 1){

                newDexByte = new byte[totalLen];//新dex的byte数组
                //添加解壳代码
                System.arraycopy(unShellDexByte, 0, newDexByte, 0, unShellDexByteLen);//先拷贝dex内容
                //添加头
                System.arraycopy(header, 0, newDexByte, unShellDexByteLen, headerLen);
                //添加dex个数
                System.arraycopy(dexNumberByte, 0, newDexByte, unShellDexByteLen + headerLen, dexNumberLen);
                //添加mainDex长度
                System.arraycopy(mainDexLenByte, 0, newDexByte, unShellDexByteLen + headerLen + dexNumberLen, mainDexLenByteLen);
                //添加加密后的解壳数据
                System.arraycopy(mainDexByte, 0, newDexByte, unShellDexByteLen + headerLen + dexNumberLen + mainDexLenByteLen, mainDexByteLen);//再在dex内容后面拷贝apk的内容

            } else {

                List<byte[]> otherDexBytes = new ArrayList<byte[]>();

                for (String path : otherPaths){

                    File otherDex = new File(path);

                    byte[] otherDexByte = readFileBytes(otherDex);
                    byte[] otherDexLenByte = intToByte2(otherDexByte.length);
                    byte[] otherDexLenFileByte = new byte[4 + otherDexByte.length];
                    System.arraycopy(otherDexLenByte, 0, otherDexLenFileByte, 0, 4);
                    System.arraycopy(otherDexByte, 0, otherDexLenFileByte, 4, otherDexByte.length);
                    totalLen = totalLen + otherDexLenFileByte.length;

                    otherDexBytes.add(otherDexLenFileByte);

                    System.out.println(">>>>" + otherDex.delete());
                }

                newDexByte = new byte[totalLen];//新dex的byte数组

                //添加解壳代码
                System.arraycopy(unShellDexByte, 0, newDexByte, 0, unShellDexByteLen);//先拷贝dex内容
                //添加头
                System.arraycopy(header, 0, newDexByte, unShellDexByteLen, headerLen);
                //添加dex个数
                System.arraycopy(dexNumberByte, 0, newDexByte, unShellDexByteLen + headerLen, dexNumberLen);
                //添加mainDex长度
                System.arraycopy(mainDexLenByte, 0, newDexByte, unShellDexByteLen + headerLen + dexNumberLen, mainDexLenByteLen);
                //添加mainDex数据
                System.arraycopy(mainDexByte, 0, newDexByte, unShellDexByteLen + headerLen + dexNumberLen + mainDexLenByteLen, mainDexByteLen);//再在dex内容后面拷贝apk的内容
                int nowPos = unShellDexByteLen + headerLen + dexNumberLen + mainDexLenByteLen + mainDexByteLen;
                //添加其他Dex数据
                for (byte[] otherDexByte : otherDexBytes){
                    System.arraycopy(otherDexByte, 0, newDexByte, nowPos, otherDexByte.length);
                    nowPos = nowPos + otherDexByte.length;
                }
            }

            fixFileSizeHeader(newDexByte);//修改DEX file size文件头
            fixSHA1Header(newDexByte);//修改DEX SHA1 文件头
            fixCheckSumHeader(newDexByte);//修改DEX CheckSum文件头

            String str = outDir + File.separator + "classes.dex";
            FileOutputStream localFileOutputStream = new FileOutputStream(str);
            localFileOutputStream.write(newDexByte);
            localFileOutputStream.flush();
            localFileOutputStream.close();

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }


    }

    public static void mergeDex(String srcPath, String shieldPath, String outDir) {
        try {
            File srcApkFile = new File(srcPath); //需要加壳的源apk
            File unShellDexFile = new File(shieldPath);//解壳用dex

            byte[] srcApkByte = readFileBytes(srcApkFile);
            byte[] unShellDexByte = readFileBytes(unShellDexFile);
            byte[] header = "wks_".getBytes("utf-8");
            byte[] fileLen = intToByte2(srcApkByte.length);

            int srcApkLen = srcApkByte.length;//源apk长
            int unShellDexLen = unShellDexByte.length;//解壳dex长
            int headerLen = header.length;//头长度
            int fLenLen = 4;

            int totalLen = unShellDexLen + srcApkLen + headerLen + fLenLen;//总长
            byte[] newDexByte = new byte[totalLen];//新dex的byte数组

            //添加解壳代码
            System.arraycopy(unShellDexByte, 0, newDexByte, 0, unShellDexLen);//先拷贝dex内容
            //添加头
            System.arraycopy(header, 0, newDexByte, unShellDexLen, headerLen);
            //添加长度
            System.arraycopy(fileLen, 0, newDexByte, unShellDexLen + headerLen, fLenLen);//最后4为长度
            //添加加密后的解壳数据
            System.arraycopy(srcApkByte, 0, newDexByte, unShellDexLen + headerLen + fLenLen, srcApkLen);//再在dex内容后面拷贝apk的内容

            fixFileSizeHeader(newDexByte);//修改DEX file size文件头
            fixSHA1Header(newDexByte);//修改DEX SHA1 文件头
            fixCheckSumHeader(newDexByte);//修改DEX CheckSum文件头

            String str = outDir + File.separator + "classes.dex";
            File newDex = new File(str);
            if (!newDex.exists()) {
                newDex.createNewFile();
            }

            FileOutputStream localFileOutputStream = new FileOutputStream(str);
            localFileOutputStream.write(newDexByte);
            localFileOutputStream.flush();
            localFileOutputStream.close();

            System.out.println(">>>>" + srcApkFile.delete());
            System.out.println(">>>>" + unShellDexFile.delete());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NoSuchAlgorithmException e){
            System.out.println(e.getMessage());
        }
    }

    private static byte[] readFileBytes(File file) throws IOException {
        byte[] arrayOfByte = new byte[1024];
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        while (true) {
            int i = fis.read(arrayOfByte);
            if (i != -1) {
                localByteArrayOutputStream.write(arrayOfByte, 0, i);
            } else {
                fis.close();
                return localByteArrayOutputStream.toByteArray();
            }
        }
    }

    private static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    private static byte[] intToByte2(int number) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    private static void fixFileSizeHeader(byte[] dexBtye) {
        //新文件长度
        byte[] newfs = intToByte(dexBtye.length);
        byte[] refs = new byte[4];
        //高位在前，低位在前掉个个
        for (int i = 0; i < 4; i++) {
            refs[i] = newfs[newfs.length - 1 - i];
        }
        System.arraycopy(refs, 0, dexBtye, 32, 4);//修改（32-35）
    }

    private static void fixCheckSumHeader(byte[] dexByte) {
        Adler32 adler = new Adler32();
        adler.update(dexByte, 12, dexByte.length - 12);//从12到文件末尾计算校验码
        long value = adler.getValue();
        int va = (int) value;
        byte[] newcs = intToByte(va);
        //高位在前，低位在前掉个个
        byte[] recs = new byte[4];
        for (int i = 0; i < 4; i++) {
            recs[i] = newcs[newcs.length - 1 - i];
        }
        System.arraycopy(recs, 0, dexByte, 8, 4);//效验码赋值（8-11）
    }

    private static void fixSHA1Header(byte[] dexByte) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(dexByte, 32, dexByte.length - 32);//从32为到结束计算sha--1
        byte[] newdt = md.digest();
        System.arraycopy(newdt, 0, dexByte, 12, 20);//修改sha-1值（12-31）
        //输出sha-1值，可有可无
        String hexstr = "";
        for (int i = 0; i < newdt.length; i++) {
            hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16)
                    .substring(1);
        }
    }
}
