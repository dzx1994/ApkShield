import com.wknight.safe.shield.engine.ApkShieldEngine;
import com.wknight.safe.shield.res.ResProGuarder;
import com.wknight.safe.shield.util.ShieldDexTool;
import com.wknight.safe.shield.util.ShieldUtil;
import com.wknight.safe.shield.util.ZipTools;
import org.junit.Test;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public class testClass {

//    @Test
    public void testzip(){
        String tmpDirPath = "tmp" + File.separator;
        File srcFile = new File("input/app.apk");
        ZipTools.unzipApk(srcFile, tmpDirPath);
    }

//    @Test
    public void testChangeAM(){
        ApkShieldEngine engine = new ApkShieldEngine();
        engine.startRebuild();
    }

//    @Test
    public void testCopySmali(){
        String tmpDirPath = "tmp" + File.separator;
        String srcSmaliDir = "files/smali" + File.separator;
        String targetSmliDir = tmpDirPath + "app/smali" + File.separator;
        try {
            ShieldUtil.copyFiles(srcSmaliDir, targetSmliDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testChangeAppName(){
        String tmpDirPath = "tmp" + File.separator;
        String targetSmliDir = tmpDirPath + "app/smali" + File.separator;
        String filePath = targetSmliDir + "com/wknight/dexshell/ProxyApplication.smali";

        ApkShieldEngine engine = new ApkShieldEngine();
//        try {
//            engine.smaliChangeApplicationName(filePath, "com.app.apppppppppp");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    @Test
    public void testZip(){
        try {
            ZipTools.zipFile("tmp/comfea/", "tmp/comfae.apk");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    @Test
    public void testRsa(){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("tmp/app/CERT.RSA");
            PKCS7 pkcs7 = new PKCS7(fis);
            X509Certificate publicKey = pkcs7.getCertificates()[0];
            String s = ShieldUtil.bytesToHexString(publicKey.getEncoded());
            System.out.println(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testGetFile(){
        String path = "tmp/YBS/assets/";
        List<File> files = ShieldUtil.getFiles(path);
        System.out.println(files);
    }

//    @Test
    public void testRes(){
        ShieldUtil.deleteDir(new File("tmp/row-dex" + File.separator + "res"));
        File tmpApkFile = new File("tmp/" + File.separator + "row-dex.apk");
        ResProGuarder resProGuarder = new ResProGuarder("tmp/row-dex", tmpApkFile);
        try {
            resProGuarder.handle_utf8();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testRepackage(){
        File apkFile = new File("input/shield.apk");
        ApkShieldEngine engine = new ApkShieldEngine(apkFile, "tmp/", "output/");
        engine.startRebuild();
    }

//    @Test
    public void testSmaliDir(){
//        ShieldUtil.deleteDir(new File())
    }

//    @Test
    public void testCopyLibs(){
        try {
            ShieldUtil.copyFiles("files/lib/", "tmp/app/lib/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testMutiDex(){
        String unzipDir = "tmp/mutiDexTest" + File.separator;
        String tmpDirPath = "tmp" + File.separator;
        ApkShieldEngine engine = new ApkShieldEngine();
//        engine.setUnzipDir(unzipDir, tmpDirPath, "com.wk.test");
        String mainDexPath = null;
//        try {
//            mainDexPath = engine.encryptMainDex();
//            List<String> otherPaths = engine.encryptOtherSrcDexs();
//            String shieldDex = engine.generateShieldDex();
//            if (mainDexPath != null && shieldDex != null){
//                ShieldDexTool.mergeDex(mainDexPath, otherPaths, shieldDex, unzipDir);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

}
