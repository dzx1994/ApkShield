package com.wknight.safe.signer;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.ArrayList;

public class SignApk {
    private ArrayList<String> signed_apk_paths;
    private static Shell sShell;
    private static CLabel processMessageLabel;
    private static Text content;
    private static String result_str;

    public SignApk()
    {
        result_str = "";
        this.signed_apk_paths = new ArrayList();
    }

    public String getresult()
    {
        return result_str;
    }

    public ArrayList<String> getSignedApkPaths()
    {
        return this.signed_apk_paths;
    }

    public static boolean deleteFile(String sPath)
    {
        boolean flag = false;
        File file = new File(sPath);
        if ((file.isFile()) && (file.exists()))
        {
            file.delete();
            flag = true;
        }
        return flag;
    }

    public void deleteDir(String filepath)
    {
        File file = new File(filepath);
        if ((file.exists()) && (file.isDirectory())) {
            if (file.listFiles().length == 0)
            {
                file.delete();
            }
            else
            {
                File[] delFile = file.listFiles();
                int i = file.listFiles().length;
                for (int j = 0; j < i; j++)
                {
                    if (delFile[j].isDirectory()) {
                        deleteDir(delFile[j].getAbsolutePath());
                    }
                    delFile[j].delete();
                }
            }
        }
        file.delete();
    }

    public int signed_apks()
            throws Exception
    {
        for (String destfileName : SignKeystore.listPath)
        {
            int index = destfileName.toString().lastIndexOf("_");
            String originalPath = destfileName.toString().substring(0, index);
            String originalFile = originalPath + ".apk";

            System.out.println(originalFile);

            ZipUtil zip = new ZipUtil();
            if (zip.unZip(originalFile))
            {
                String zipcmd =
                        ".\\libs\\java\\bin\\7za.exe -tzip -ssc d " +
                                originalFile.toString().replace(" ", "\" \"") + " " +
                                "META-INF";

                Runtime rn = Runtime.getRuntime();
                Process proc = null;

                proc = rn.exec(zipcmd);
                InputStream stdin = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);

                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                int exitVal = proc.waitFor();

                System.out.println(exitVal);

                String file = "";
                int _index = destfileName.toString().lastIndexOf("_");
                if (_index != -1)
                {
                    file = destfileName.toString().substring(0, _index);
                    file = file + ".apk";
                }
                else
                {
                    file = destfileName;
                }
                if (exitVal != 0) {
                    if (exitVal == 2)
                    {
                        result_str = result_str + file + "  读取压缩文件失败，可能被其余进程占用，关闭后重试\r\n";
                        return exitVal;
                    }
                }
            }
        }
        return 0;
    }

    public static boolean sign_one_apk(Shell shell, String destPath, final Button okButton, SelectionEvent e)
    {
        System.out.println("in sign one apk, desPath:" + destPath + "size:" + SignKeystore.listPath.size());
        if ((destPath != "") && (SignKeystore.listPath.size() == 1))
        {
            int index = ((String)SignKeystore.listPath.get(0)).toString().lastIndexOf("_");
            String originalFile = ((String)SignKeystore.listPath.get(0)).toString().substring(0, index);
            originalFile = originalFile + ".apk";

            int dotIndex = destPath.toString().lastIndexOf(".");
            String alignedname = destPath.toString().substring(0, dotIndex);

            String alignedFile = alignedname +
                    "_Aligned.apk";

            String exeAlignFile = ".\\libs\\java\\bin\\zipalign.exe -f -v 4 " +
                    destPath.toString().replace(" ", "\" \"") +
                    " " + alignedFile.replace(" ", "\" \"");

            String exeFile =
                    ".\\libs\\java\\bin\\jarsigner.exe -verbose -keystore " +
                            SignKeystore.KEYSTORE_PATH.replace(" ", "\" \"") +
                            " -storepass " + "\"" + SignKeystore.KEYSTORE_PWD +
                            "\"" + " -keypass " + "\"" + SignKeystore.KEY_PWD +
                            "\"" + " -signedjar " + destPath.toString().replace(" ", "\" \"") + " " +
                            originalFile.replace(" ", "\" \"") + " " + "\"" +
                            SignKeystore.KEY_ALIAS + "\"";

            execute(exeFile, exeAlignFile, destPath, shell, e);

            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    okButton.setEnabled(true);
                }
            });
            return true;
        }
        return false;
    }

    public static void sign_multi_apks(Shell shell, String destPath, final Button okButton, SelectionEvent e, int i)
    {
        String destfileName = (String)SignKeystore.listPath.get(i);

        int index = destfileName.toString().lastIndexOf("_");
        String originalFile = destfileName.toString().substring(0, index);
        originalFile = originalFile + ".apk";

        int dotIndex = destfileName.toString().lastIndexOf(".");
        String alignedname = destfileName.toString().substring(0, dotIndex);

        String alignedFile = alignedname +
                "_Aligned.apk";

        String exeAlignFile = ".\\libs\\java\\bin\\zipalign.exe -f -v 4 " +
                destfileName.toString().replace(" ", "\" \"") +
                " " + alignedFile.replace(" ", "\" \"");

        String exeFile =
                ".\\libs\\java\\bin\\jarsigner.exe -verbose -keystore " +
                        SignKeystore.KEYSTORE_PATH.replace(" ", "\" \"") +
                        " -storepass " + "\"" + SignKeystore.KEYSTORE_PWD +
                        "\"" + " -keypass " + "\"" + SignKeystore.KEY_PWD +
                        "\"" + " -signedjar " + destfileName.toString().replace(" ", "\" \"") + " " +
                        originalFile.replace(" ", "\" \"") + " " + "\"" +
                        SignKeystore.KEY_ALIAS + "\"";




        execute(exeFile, exeAlignFile, destfileName, shell, e);


        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                okButton.setEnabled(true);
            }
        });
    }

    public static void sign_by_command(Shell shell, String destPath, Button okButton, SelectionEvent e)
            throws Exception
    {
        createSShell(shell, destPath, okButton, e);
    }

    public static void execute(String exeFile, String exeAlignFile, final String destfileName, Shell shell, SelectionEvent e)
    {
        Runtime rn = Runtime.getRuntime();
        Process proc = null;
        Process alignproc = null;
        try
        {
            proc = rn.exec(exeFile);
            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            final int exitVal = proc.waitFor();

            alignproc = rn.exec(exeAlignFile);
            InputStream stdin02 = alignproc.getInputStream();
            InputStreamReader isr02 = new InputStreamReader(stdin02);
            BufferedReader br02 = new BufferedReader(isr02);

            String line02 = null;
            while ((line02 = br02.readLine()) != null) {
                System.out.println(line02);
            }
            final int exitVal02 = alignproc.waitFor();

            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    int index = destfileName.lastIndexOf("_");
                    String file;
                    if (index != -1)
                    {
                        file = destfileName.substring(0, index) + ".apk";
                    }
                    else
                    {
                        file = destfileName;
                    }
                    if ((exitVal == 0) && (exitVal02 == 0))
                    {
                        SignApk.deleteFile(destfileName);
                        SignApk.result_str = SignApk.result_str + file + " 签名成功\r\n";
                        SignApk.content.setText(SignApk.result_str);
                    }
                    else if ((exitVal == 0) && (exitVal02 != 0))
                    {
                        SignApk.result_str = SignApk.result_str + file + " 对齐失败\r\n";
                        SignApk.content.setText(SignApk.result_str);
                    }
                    else
                    {
                        SignApk.deleteFile(destfileName);
                        SignApk.result_str = SignApk.result_str + file + " 签名失败（请检查原包是否正常 或者 输入的第二个密码是否正确）\r\n";
                        SignApk.content.setText(SignApk.result_str);
                    }
                }
            });
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    public static void createSShell(Shell shell, final String destPath, final Button okButton, final SelectionEvent e)
    {
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                SignApk.sShell = new Shell(Display.getDefault(), 84064);
                SignApk.sShell.setText("进度");
                SignApk.sShell.setSize(300, 200);
                RowLayout layout = new RowLayout(512);
                layout.spacing = 0;
                layout.marginLeft = 0;
                layout.marginTop = 0;
                SignApk.sShell.setLayout(layout);

                SignApk.progress(Display.getDefault(), SignKeystore.listPath.size(), SignApk.sShell, destPath, okButton, e);
                SignApk.sShell.pack();
                SignApk.sShell.open();
                while (!SignApk.sShell.isDisposed()) {
                    if (!Display.getDefault().readAndDispatch()) {
                        Display.getDefault().sleep();
                    }
                }
            }
        });
    }

    public static ProgressBar progress(Display display, final int max, final Shell shell, final String destPath, final Button okButton, final SelectionEvent e)
    {
        String processMessage = "processing......";

        CLabel message = new CLabel(sShell, 8);
        message.setText(processMessage);

        message.setLayoutData(new RowData(330, 20));

        final ProgressBar progress = new ProgressBar(sShell, 65544);
        progress.setLayoutData(new RowData(330, 20));

        processMessageLabel = new CLabel(sShell, 8);
        processMessageLabel.setLayoutData(new RowData(330, 20));

        content = new Text(sShell, 840);

        content.setLayoutData(new RowData(300, 100));

        progress.setMaximum(max);
        progress.setMinimum(0);
        final int Maximum = progress.getMaximum();
        final int Minimum = progress.getMinimum();

        Runnable Run = new Runnable()
        {
            public void run()
            {
                for (int i = Minimum; i < Maximum; i++)
                {
                    System.out.println("desPath:" + destPath);
                    if (SignApk.sign_one_apk(shell, destPath, okButton, e))
                    {
                        Display.getDefault().asyncExec(new Runnable()
                        {
                            public void run()
                            {
                                if (progress.isDisposed()) {
                                    return;
                                }
                                progress.setSelection(progress.getSelection() + 1);
                                String info = SignApk.process(progress.getSelection());
                                SignApk.processMessageLabel.setText(info);
                            }
                        });
                        return;
                    }
                    SignApk.sign_multi_apks(shell, destPath, okButton, e, i);

                    Display.getDefault().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            if (progress.isDisposed()) {
                                return;
                            }
                            progress.setSelection(progress.getSelection() + 1);
                            String info = SignApk.process(progress.getSelection());
                            SignApk.processMessageLabel.setText(info);
                            try
                            {
                                Thread.sleep(2000L);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            if (progress.getSelection() == max)
                            {
                                final MessageBox messageBox = new MessageBox(SignApk.sShell, 40);


                                Display.getDefault().asyncExec(new Runnable()
                                {
                                    public void run()
                                    {
                                        SignApk.WriteMyFile();
                                        messageBox.setText("");
                                        messageBox.setMessage("Done!");

                                        e.doit = (messageBox.open() == 64);
                                        SignApk.close();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        };
        new Thread(Run).start();
        return progress;
    }

    public static void WriteMyFile()
    {
        try
        {
            FileWriter fw = new FileWriter(".\\log.txt");
            PrintWriter out = new PrintWriter(fw);
            out.print(result_str);
            out.close();
            fw.close();
        }
        catch (IOException e)
        {
            System.out.println("写入错误");
            e.printStackTrace();
        }
    }

    public static void close()
    {
        Display.getDefault().syncExec(new Runnable()
        {
            public void run()
            {
                SignApk.sShell.close();
                SignApk.sShell.dispose();
            }
        });
    }

    private static String process(int selection)
    {
        String str = " apk processed ";
        String str_int = Integer.toString(selection);
        str_int = str_int + str;
        return str_int;
    }
}
