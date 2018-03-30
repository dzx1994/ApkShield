package com.wknight.safe.shield.engine;


import com.wknight.safe.shield.Interface.IWKnight_shieldEngine;
import com.wknight.safe.shield.parser.ManifestParser;
import com.wknight.safe.shield.res.ResProGuarder;
import com.wknight.safe.shield.util.AxmlUtil;
import com.wknight.safe.shield.util.DexBuilder;
import com.wknight.safe.shield.util.PackUtil;
import com.wknight.safe.shield.util.ShieldDexTool;
import com.wknight.safe.shield.util.ShieldUtil;
import com.wknight.safe.shield.util.ZipTools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import SevenZip.LzmaAlone;
import pxb.android.axml.AxmlReader;
import sun.security.pkcs.PKCS7;

public class ApkShieldEngine implements IWKnight_shieldEngine {
    private File apkFile;
    private File tmpApkFile;
    private String tmpDirPath;
    private String outPath;
    private String unzipDir;
    private String ori_appName;
    private final String appName = "com.wknight.dexshell.ProxyApplication";
    private final String srcLibsDir = "files" + File.separator + "lib" + File.separator;
    private final String srcSmaliDir = "files" + File.separator + "smali" + File.separator;
    private final String shieldPackagePath = "com" + File.separator + "wknight" + File.separator + "dexshell" + File.separator;
    private String tmpSmaliDir;
    private Map<String, String> changeMap;
    private List<String> cryptedAss;

    public ApkShieldEngine() {

    }

    public ApkShieldEngine(File apkFile, String tmpPath, String outPath) {
        this.apkFile = apkFile;
        this.tmpApkFile = new File(tmpPath + apkFile.getName());
        this.tmpDirPath = tmpPath;
        this.outPath = outPath;

        File tmpDir = new File(tmpDirPath);
        File outDir = new File(outPath);
        if (!tmpDir.exists()) tmpDir.mkdir();
        if (!outDir.exists()) outDir.mkdir();
    }

    @Override
    public void startRebuild() {
        try {
            //0.copy apk file to tmp dir 先把apk文件复制到temp目录，所有操作在temp目录下进行
            ShieldUtil.copyFile(apkFile, tmpApkFile);
            //1.find origin application name 获取原始application 名称保存下来
            ori_appName = getOriAppName(tmpApkFile);
            //2.unzip zpk file  解压temp中的apk文件
            unzipDir = unzipApk(tmpApkFile, tmpDirPath);
            //3.res proGuard
            //===================资源混淆开始=============
            ShieldUtil.deleteDir(new File(unzipDir + File.separator + "res"));//删除解压以后res文件夹
            ResProGuarder resProGuarder = new ResProGuarder(unzipDir, tmpApkFile);//新建一个ResProGuarder
            resProGuarder.handle_utf8();
            changeMap = resProGuarder.getChangeMap();
            //===================资源混淆结束===============
            //4.encrypt picture file in assets dir

           // cryptedAss = shieldAssetFiles(); 暂时关闭资源文件加密

            //5.change AndroidManifest.xml file
            modifyAxmlAttr(unzipDir, appName);
            //6.delete apk file in tmp dir
            System.out.println(tmpApkFile.delete());
            //7.shield and compress origin dex file
            String mainDexPath = encryptMainDex();
            List<String> otherPaths = encryptOtherSrcDexs();
            String shieldDex = generateShieldDex();
            if (mainDexPath != null && shieldDex != null) {
                ShieldDexTool.mergeDex(mainDexPath, otherPaths, shieldDex, unzipDir);
            }
            //8.copy library and compress with upx
            copyLibsFiles();
            //9.repackage apk file
            ZipTools.zipFile(unzipDir, tmpApkFile.getPath());
            //10.delete tmp files
            ShieldUtil.deleteDir(new File(tmpSmaliDir));
            File outputFile = new File(outPath + tmpApkFile.getName());
            System.out.println(tmpApkFile.renameTo(outputFile));
            ShieldUtil.deleteDir(new File(tmpDirPath));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * get AndroidManifest.xml and classes.dex from apk file
     *
     * @param srcFile apk file to unzip
     * @param tarPath output directory path
     * @return path of directory which include AndroidManifest.xml and classes.dex
     */
    private String unzipApk(File srcFile, String tarPath) {
        String unzipPath = ZipTools.unzipApk(srcFile, tarPath);
        File assets = new File(unzipPath + "assets");
        if (!assets.exists()) assets.mkdirs();
        return unzipPath;
    }

    /**
     * get Application name in AndroidManifest.xml
     *
     * @param apk apk file which to get App name
     * @return application name in AndroidManifest.xml
     */
    private String getOriAppName(File apk) {

        String cmd;
        String ori_appName = "";
        Properties props = System.getProperties();
        String osName = props.getProperty("os.name");
        //获取使用的环境是 linxu还是 windows
        if (osName.equals("Linux")) {
            cmd = String.format("bin" + File.separator + "aapt dump xmltree %s AndroidManifest.xml", apk.getPath());
        } else {
            cmd = String.format("bin" + File.separator + "aapt.exe dump xmltree %s AndroidManifest.xml", apk.getPath());
        }

        Runtime run = Runtime.getRuntime();
        try {
            Process p = run.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            Map<String, String> map = ManifestParser.findPkgNameAppName(inBr);
            String appNameInManifest = map.get("appName");
            String pkgNameInManifest = map.get("pkgName");

            if (appNameInManifest.equals("")) {
                ori_appName = "";
            } else if ((appNameInManifest.lastIndexOf('.') > 0) && appNameInManifest.contains(".")) {
                ori_appName = appNameInManifest;
            } else if (appNameInManifest.contains(".") && (appNameInManifest.lastIndexOf('.') == 0)) {
                ori_appName = pkgNameInManifest + appNameInManifest;
            } else if ((appNameInManifest.lastIndexOf('.') < 0)) {
                ori_appName = pkgNameInManifest + "." + appNameInManifest;
            }

            //检查命令是否执行失败。
            if (p.waitFor() != 0) {
                //p.exitValue()==0表示正常结束，1：非正常结束
                if (p.exitValue() == 1)
                    System.err.println("aapt命令执行失败!");
            }

            inBr.close();
            in.close();
        } catch (InterruptedException e) {
            System.out.println("修复manifest.xml app名 被中断");
        } catch (IOException e) {
            System.out.println("fix application error：" + e.getMessage());
        }

        return ori_appName;
    }

    /**
     * change Application name in AndroidManifest.xml to proxyApplication name
     *
     * @param filePath        path of AndroidManifest.xml
     * @param applicationName new application name
     * @throws Exception
     */
    private void changeManifestApplicationName(String filePath, String applicationName) throws Exception {
        File manifestFile = null;
        try {
            manifestFile = new File(filePath);
            InputStream is = new FileInputStream(manifestFile);
            byte[] xml = new byte[is.available()];
            is.read(xml);
            is.close();

            AxmlReader xmlReader = new AxmlReader(xml);
            xmlReader.preprocess(applicationName);

            if ((AxmlReader.mFileSizeOffset >= AxmlReader.mStringChunkSizeOffset) || (
                    (AxmlReader.mStringChunkSizeOffset >= AxmlReader.mApplicationAttrSizeOffset) &&
                            (AxmlReader.mStringChunkSizeOffset >= AxmlReader.mAppNameValueIndexOffset))) {
                throw new Exception("The data is bad, XML chunk before String chunk");
            }

            int offset;
            int len;
            int lastFieldEndPos;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            offset = 0;
            len = AxmlReader.mFileSizeOffset;
            outputStream.write(xml, offset, len);
            offset += len;

            AxmlReader.writeInt(outputStream, AxmlReader.mFileSize);
            offset += 4;
            lastFieldEndPos = AxmlReader.mFileSizeOffset + 4;

            if (AxmlReader.mStringChunkSizeOffset > lastFieldEndPos) {
                len = AxmlReader.mStringChunkSizeOffset - lastFieldEndPos;
                outputStream.write(xml, offset, len);
                offset += len;
            }

            AxmlReader.writeInt(outputStream, AxmlReader.mStringChunkSize);
            offset += 4;
            lastFieldEndPos = AxmlReader.mStringChunkSizeOffset + 4;

            if (AxmlReader.mStringCountOffset > lastFieldEndPos) {
                len = AxmlReader.mStringCountOffset - lastFieldEndPos;
                outputStream.write(xml, offset, len);
                offset += len;
            }
            AxmlReader.writeInt(outputStream, AxmlReader.mStringCount);
            offset += 4;
            lastFieldEndPos = AxmlReader.mStringCountOffset + 4;

            if (AxmlReader.mStringDataOffsetOffset > lastFieldEndPos) {
                len = AxmlReader.mStringDataOffsetOffset - lastFieldEndPos;
                outputStream.write(xml, offset, len);
                offset += len;
            }
            AxmlReader.writeInt(outputStream, AxmlReader.mStringDataOffset);
            offset += 4;
            lastFieldEndPos = AxmlReader.mStringDataOffsetOffset + 4;

            if (AxmlReader.mStringAppNameOffsetOffset > lastFieldEndPos) {
                len = AxmlReader.mStringAppNameOffsetOffset - lastFieldEndPos;
                outputStream.write(xml, offset, len);
                offset += len;
            }
            AxmlReader.writeInt(outputStream, AxmlReader.mStringAppNameOffset);
            lastFieldEndPos = AxmlReader.mStringAppNameOffsetOffset + 4;

            if (AxmlReader.mStringAppNameDataOffset > lastFieldEndPos) {
                len = AxmlReader.mStringAppNameDataOffset - lastFieldEndPos;
                outputStream.write(xml, offset, len);
                offset += len;
            }
            outputStream.write(AxmlReader.mStringAppNameData);
            lastFieldEndPos = AxmlReader.mStringAppNameDataOffset + AxmlReader.mStringAppNameData.length;

            if (AxmlReader.mAttrAppNameDataOffset >= 0) {
                if (AxmlReader.mApplicationAttrSizeOffset > lastFieldEndPos) {
                    len = AxmlReader.mApplicationAttrSizeOffset - lastFieldEndPos;
                    outputStream.write(xml, offset, len);
                    offset += len;
                }
                AxmlReader.writeInt(outputStream, AxmlReader.mApplicationAttrSize);
                offset += 4;
                lastFieldEndPos = AxmlReader.mApplicationAttrSizeOffset + 4;

                if (AxmlReader.mApplicationAttrCountOffset > lastFieldEndPos) {
                    len = AxmlReader.mApplicationAttrCountOffset - lastFieldEndPos;
                    outputStream.write(xml, offset, len);
                    offset += len;
                }
                AxmlReader.writeInt(outputStream, AxmlReader.mApplicationAttrCount);
                offset += 4;
                lastFieldEndPos = AxmlReader.mApplicationAttrCountOffset + 4;

                if (AxmlReader.mAttrAppNameDataOffset > lastFieldEndPos) {
                    len = AxmlReader.mAttrAppNameDataOffset - lastFieldEndPos;
                    outputStream.write(xml, offset, len);
                    offset += len;
                }
                outputStream.write(AxmlReader.mAttrAppNameData);
                lastFieldEndPos = AxmlReader.mAttrAppNameDataOffset + AxmlReader.mAttrAppNameData.length;
            } else {
                if (AxmlReader.mAppNameValueUnknownOffset > lastFieldEndPos) {
                    len = AxmlReader.mAppNameValueUnknownOffset - lastFieldEndPos;
                    outputStream.write(xml, offset, len);
                    offset += len;
                }
                AxmlReader.writeInt(outputStream, AxmlReader.mAppNameValueUnknown);
                offset += 4;
                lastFieldEndPos = AxmlReader.mAppNameValueUnknownOffset + 4;

                if (AxmlReader.mAppNameValueTypeOffset > lastFieldEndPos) {
                    len = AxmlReader.mAppNameValueTypeOffset - lastFieldEndPos;
                    outputStream.write(xml, offset, len);
                    offset += len;
                }
                AxmlReader.writeInt(outputStream, AxmlReader.mAppNameValueType);
                offset += 4;
                lastFieldEndPos = AxmlReader.mAppNameValueTypeOffset + 4;

                if (AxmlReader.mAppNameValueIndexOffset > lastFieldEndPos) {
                    len = AxmlReader.mAppNameValueIndexOffset - lastFieldEndPos;
                    outputStream.write(xml, offset, len);
                    offset += len;
                }
                AxmlReader.writeInt(outputStream, AxmlReader.mAppNameValueIndex);
                offset += 4;
                lastFieldEndPos = AxmlReader.mAppNameValueIndexOffset + 4;
            }

            if (xml.length > offset) {
                outputStream.write(xml, offset, xml.length - offset);
            }

            manifestFile.delete();
            manifestFile.createNewFile();
            byte[] modified = outputStream.toByteArray();
            FileOutputStream fos = new FileOutputStream(manifestFile);
            fos.write(modified);
            fos.close();
        } catch (Exception e) {
            System.out.println("changeManifestApplicationName failed, delete the AndroidManifest.xml");
            if (manifestFile != null) {
                manifestFile.delete();
            }
            throw e;
        }
    }

    // 修改 manifest 文件
    private void modifyAxmlAttr(String unzipDir, String appName) {
        String ameditor = "ameditor.exe";
        String manifest = unzipDir + "AndroidManifest.xml";
        String tmp = unzipDir + "tmp.xml";

        int flag = 0;
        try {
            flag = AxmlUtil.hasAllowBackupAndDebuggableAndName(unzipDir + "AndroidManifest.xml");
        } catch (IOException e) {
            System.out.println("fix manifest.xml attr error：" + e.getMessage());
        }

        String modifyAppName = String.format("bin" + File.separator + ameditor + " attr --modify application -d 1 -n name -t 3 -v %s -i %s -o %s", appName, manifest, tmp);

        String modifyDebug = String.format("bin" + File.separator + ameditor + " attr --modify application -d 1 -n debuggable -t 3 -v false -i %s -o %s", manifest, tmp);
        String addDebug = String.format("bin" + File.separator + ameditor + " attr --add application -d 1 -n debuggable -t 3 -v false -i %s -o %s", manifest, tmp);

        String modifyBackup = String.format("bin" + File.separator + ameditor + " attr --modify application -d 1 -n allowBackup -t 3 -v false -i %s -o %s", manifest, tmp);
        String addBackup = String.format("bin" + File.separator + ameditor + " attr --add application -d 1 -n allowBackup -t 3 -v false -i %s -o %s", manifest, tmp);

        String cmd = null;
        String cmd2 = null;
        String cmd3 = null;

        if ((flag & 4) == 0) {
            try {
                changeManifestApplicationName(unzipDir + "AndroidManifest.xml", appName);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            cmd = modifyAppName;
        }

        if ((flag & 1) == 0) {
            cmd2 = addDebug;
        } else {
            cmd2 = modifyDebug;
        }
        if ((flag & 2) == 0) {
            cmd3 = addBackup;
        } else {
            cmd3 = modifyBackup;
        }

        if (cmd2 == null || cmd3 == null) throw new RuntimeException("modify axml fail");

        Runtime run = Runtime.getRuntime();
        if (cmd != null) {
            try {
                Process p = run.exec(cmd);

                //检查命令是否执行失败。
                if (p.waitFor() != 0) {
                    //p.exitValue()==0表示正常结束，1：非正常结束
                    if (p.exitValue() == 1)
                        System.err.println("ameditor命令执行失败!");
                }
                File srcFile = new File(manifest);
                File tmpFile = new File(tmp);
                srcFile.delete();
                tmpFile.renameTo(srcFile);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            Process p = run.exec(cmd2);

            //检查命令是否执行失败。
            if (p.waitFor() != 0) {
                //p.exitValue()==0表示正常结束，1：非正常结束
                if (p.exitValue() == 1)
                    System.err.println("ameditor命令执行失败!");
            }
            File srcFile = new File(manifest);
            File tmpFile = new File(tmp);
            srcFile.delete();
            tmpFile.renameTo(srcFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            Process p = run.exec(cmd3);

            //检查命令是否执行失败。
            if (p.waitFor() != 0) {
                //p.exitValue()==0表示正常结束，1：非正常结束
                if (p.exitValue() == 1)
                    System.err.println("ameditor命令执行失败!");
            }
            File srcFile = new File(manifest);
            File tmpFile = new File(tmp);
            srcFile.delete();
            tmpFile.renameTo(srcFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private List<String> shieldAssetFiles() {
        String path = unzipDir + File.separator + "assets";
        List<File> files = ShieldUtil.getFiles(path);
        List<String> cryptedPath = new ArrayList<String>();
        for (File file : files) {
            int type = ShieldUtil.getPicType(file);
            if (type == ShieldUtil.TYPE_JPG || type == ShieldUtil.TYPE_PNG) {
                PackUtil.rc4Crypt(3, new String[]{"encrypt", file.getAbsolutePath(), file.getAbsolutePath()});
                cryptedPath.add(file.getPath().replace(unzipDir, "").replace("\\", "/"));
            }
        }
        return cryptedPath;
    }

    private String generateShieldDex() {
        //copy smali files to tmp/apkname/smali
        try {
            tmpSmaliDir = tmpDirPath + "smali" + File.separator;
            ShieldUtil.copyFiles(srcSmaliDir, tmpSmaliDir);
            String applicationSmaliFile = tmpSmaliDir + shieldPackagePath + "ProxyApplication.smali";
            smaliChangeApplicationName(applicationSmaliFile, ori_appName);
            String shield = buildShieldDex(tmpSmaliDir);
            ShieldUtil.deleteDir(new File(tmpSmaliDir));
            return shield;
        } catch (IOException e) {
            return null;
        }
    }

    private void copyLibsFiles() throws Exception {
        String tarLibPath = unzipDir + "assets" + File.separator;
        File tarLibDir = new File(tarLibPath);
        if (!tarLibDir.exists()) {
            tarLibDir.mkdirs();
        } else if (tarLibDir.exists() && !tarLibDir.isDirectory()) {
            tarLibDir.delete();
            tarLibDir.mkdirs();
        }

        String apkLibPath = unzipDir + "lib" + File.separator;
        File apkLibDir = new File(apkLibPath);
        File[] libAbdis;
        if (apkLibDir.exists()) {
            libAbdis = apkLibDir.listFiles();
        } else {
            libAbdis = new File(srcLibsDir).listFiles();

            if (libAbdis != null) {
                for (File libAbdi : libAbdis) {
                    File dir = new File(apkLibPath + libAbdi.getName());
                    if (dir.exists())
                        ShieldUtil.deleteDir(dir);
                    dir.mkdirs();
                    File fakeSo = new File(apkLibPath + libAbdi.getName() + File.separator + "libWKnightShield.so");
                    fakeSo.createNewFile();
                }
            }
        }

        if (libAbdis != null) {
            for (File libAbdi : libAbdis) {
                if (libAbdi.getName().contains("64") || libAbdi.getName().contains("mips")) {
                    boolean flag = ShieldUtil.deleteDir(new File(apkLibPath + libAbdi.getName()));
                    System.out.println("delete so:" + flag);
                }
            }
            String srcSo = srcLibsDir + File.separator + "armeabi" + File.separator + "libWKnightShield.so";
            String destSo = tarLibPath + File.separator + "libWKnightShield.so";
            ShieldUtil.copyFile(srcSo, destSo);
            PackUtil.equip_shield(2, new String[]{"upx", destSo});

            srcSo = srcLibsDir + File.separator + "x86" + File.separator + "libWKnightShield.so";
            destSo = tarLibPath + File.separator + "libWKnightShield_x86.so";
            ShieldUtil.copyFile(srcSo, destSo);
            PackUtil.equip_shield(2, new String[]{"upx", destSo});
        }
        compressRSAFile();
        ShieldUtil.deleteDir(new File(unzipDir + "META-INF"));
    }

    private String compressRSAFile() throws Exception {
        String metaDirPath = unzipDir + "META-INF" + File.separator;
        File[] metas = (new File(metaDirPath)).listFiles();
        String srcPath = null;
        String compressPath = null;
        for (File f : metas) {
            if (f.getName().endsWith(".RSA")) {
                srcPath = unzipDir + "META-INF" + File.separator + f.getName();
                FileInputStream fis = new FileInputStream(srcPath);
                PKCS7 pkcs7 = new PKCS7(fis);
                X509Certificate publicKey = pkcs7.getCertificates()[0];
                FileOutputStream fos = new FileOutputStream(metaDirPath + "wknight.der");
                fos.write(publicKey.getEncoded());
                fos.flush();
                fos.close();
                fis.close();
                srcPath = metaDirPath + "wknight.der";
                compressPath = unzipDir + "assets" + File.separator + "wknight_c.dat";
            }
        }

        if (srcPath == null) return null;

        String[] args = new String[]{"e", srcPath, compressPath};
        LzmaAlone.compress(args);
        return compressPath;
    }

    private void smaliChangeApplicationName(String smaliFilePath, String ori_appName) throws IOException {
        File applicationFile = new File(smaliFilePath);
        FileInputStream fis = new FileInputStream(applicationFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

        String line;
        StringBuffer newApplication = new StringBuffer();
        while ((line = bufferedReader.readLine()) != null) {

            if (line.contains("<APK_APPLICATION_NAME>")) {
                line = line.replace("<APK_APPLICATION_NAME>", ori_appName);
            }
            newApplication.append(line).append("\n");
        }
        fis.close();
        bufferedReader.close();

        FileOutputStream fos = new FileOutputStream(applicationFile);
        fos.write(newApplication.toString().getBytes());
        fos.flush();
        fos.close();
    }

    private String encryptMainDex() throws Exception {
        String mainSrc = unzipDir + "classes.dex";
        String mainCompress = unzipDir + "classes_c.dex";
        String maintarget = unzipDir + "classes.dat";
        return encryptSrcDex(mainSrc, mainCompress, maintarget);
    }

    private List<String> encryptOtherSrcDexs() throws Exception {

        List<String> encryptPaths = new ArrayList<String>();

        File[] unzipFiles = (new File(unzipDir)).listFiles();
        if (unzipFiles == null || unzipFiles.length == 0)
            throw new Exception("empty unzip dir");

        for (File src : unzipFiles) {
            String srcPath;
            String compressPath;
            String targetPath;

            if (src.getName().contains("classes") &&
                    !src.getName().equals("classes.dex") &&
                    !src.getName().equals("classes.dat")) {
                srcPath = src.getPath();
                compressPath = unzipDir + src.getName() + "_c";
                targetPath = unzipDir + src.getName().replace(".dex", ".dat");

                targetPath = encryptSrcDex(srcPath, compressPath, targetPath);
            } else {
                continue;
            }

            encryptPaths.add(targetPath);

            File srcDex = new File(srcPath);
            srcDex.delete();
            File compressFile = new File(compressPath);
            compressFile.delete();
        }

        return encryptPaths;
    }

    private String encryptSrcDex(String srcPath, String compressPath, String targetPath) throws Exception {

        String[] args = new String[]{"e", srcPath, compressPath};
        LzmaAlone.compress(args);
        PackUtil.rc4Crypt(3, new String[]{"encrypt", compressPath, targetPath});

        File srcDex = new File(srcPath);
        srcDex.delete();
        File compressFile = new File(compressPath);
        compressFile.delete();

        return targetPath;
    }

    private String buildShieldDex(String tmpSmaliDir) {
        String targetPath = unzipDir + "shield.dex";
        DexBuilder builder = new DexBuilder(tmpSmaliDir, targetPath);
        builder.buildDex();
        return targetPath;
    }
}
