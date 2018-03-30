package com.wknight.safe.signer;


import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class Signer {
    protected Shell shell;
    private Composite mTopComposite;
    private Label mUseExistingKeystore;
    private Label mKeyAliasesLabel;
    private Label mKeyPasswordLabel;
    private Label mDestinationMsg;
    private Text mProjectText;
    private Text mKeystore;
    private Text mKeystorePassword;
    private Text mKeyPassword;
    private Text mDestination;
    private Combo mKeyAliases;

    private String finalString;
    private String destValue;

    public static void main(String[] args) {
        Signer window = new Signer();
        window.destValue = "";
        window.finalString = "";
        window.open();
    }

    public void open() {
        Display display = Display.getDefault();
        createWindow();
        shell.open();
        shell.setText("签名工具");
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    protected void createWindow() {
        shell = new Shell();
        shell.setSize(400, 400);
        shell.setLayout(new GridLayout());
        GridLayout gl = null;
        GridData gd = null;
        this.mTopComposite = new Composite(this.shell, 0);
        this.mTopComposite.setLayoutData(new GridData(1800));
        this.mTopComposite.setLayout(new GridLayout(3, false));

        Composite projectComposite = new Composite(this.mTopComposite, 0);
        projectComposite.setLayoutData(new GridData(1800));
        projectComposite.setLayout(gl = new GridLayout(3, false));

        gl.marginHeight = 0;
        gl.marginWidth = 0;
        Label label = new Label(projectComposite, 0);
        label.setLayoutData(gd = new GridData(1800));
        gd.horizontalSpan = 3;
        label.setText("请选择需要签名的文件:");
        this.mProjectText = new Text(projectComposite, 2048);
        this.mProjectText.setLayoutData(gd = new GridData(1800));
        this.mProjectText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                SignKeystore.setApkPath(Signer.this.mProjectText.getText().trim());
            }
        });
        final Button browseButton = new Button(projectComposite, 8);
        browseButton.setText("请选择");
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FilePath fp = new FilePath();
                fp.getMuitiFilePath(browseButton);

                String destfile = fp.getDestFiles();
                String srcfile = fp.getSrcFiles();
                if ((srcfile.length() > 5100) && (destfile.length() > 5100)) {
                    srcfile = srcfile.substring(0, 1000);
                    destfile = destfile.substring(0, 1000);
                }
                if (Signer.this.mDestination != null) {
                    Signer.this.mDestination.setText(destfile);
                }
                Signer.this.mProjectText.setText(srcfile);
                SignKeystore.setDestPath(fp.getDestFilePath());
                SignKeystore.setFileArray(fp.getFileName());
            }
        });
        Composite composite = new Composite(this.shell, 0);
        composite.setLayoutData(new GridData(1800));
        GridLayout gl2 = new GridLayout(3, false);
        composite.setLayout(gl2);
        this.mUseExistingKeystore = new Label(composite, 0);
        this.mUseExistingKeystore.setText("请选择Keystore的路径:");
        GridData gd2;
        this.mUseExistingKeystore.setLayoutData(gd2 = new GridData(768));
        gd2.horizontalSpan = 3;
        new Label(composite, 0).setText("路径:");

        this.mKeystore = new Text(composite, 2048);
        this.mKeystore.setLayoutData(gd2 = new GridData(768));
        String inifile = null;
        try {
            inifile = getProjectPath();

            inifile = inifile + "\\signer.ini";
            SignKeystore.INIFILENAME = inifile;
        } catch (UnsupportedEncodingException e3) {
            e3.printStackTrace();
        }
        ProfileStringMgr reader = new ProfileStringMgr();
        try {
            String pathfileName = reader.getProfileString(inifile, "KEYSTORE", "path", null);
            if (pathfileName != null) {
                this.mKeystore.setText(pathfileName);
                SignKeystore.KEYSTORE_PATH = pathfileName;
                if (this.mKeystorePassword != null) {
                    this.mKeystorePassword.setText("");
                }
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        final Button browseButton2 = new Button(composite, 8);
        browseButton2.setText("请选择");
        browseButton2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(browseButton2.getShell(), 4096);
                fileDialog.setText("请选择keystore");
                String fileName = fileDialog.open();
                if (fileName != null) {
                    Signer.this.mKeystore.setText(fileName);
                    ProfileStringMgr reader = new ProfileStringMgr();
                    try {
                        boolean bset = false;
                        bset = reader.setProfileString(SignKeystore.INIFILENAME, "KEYSTORE", "path", fileName);
                        if (!bset) {
                            System.out.print("WRITE INI FAILDED");
                        }
                        if (Signer.this.mKeystorePassword != null) {
                            Signer.this.mKeystorePassword.setText("");
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    if (Signer.this.mKeystorePassword != null) {
                        Signer.this.mKeystorePassword.setText("");
                    }
                }
            }
        });
        new Label(composite, 0).setText("密码:");
        this.mKeystorePassword = new Text(composite, 4196352);
        this.mKeystorePassword.setLayoutData(gd2 = new GridData(768));
        new Composite(composite, 0).setLayoutData(gd2 = new GridData());
        gd2.heightHint = (gd2.widthHint = 0);
        this.mKeyAliasesLabel = new Label(composite, 0);
        this.mKeyAliasesLabel.setText("别名:");
        this.mKeyAliases = new Combo(composite, 8);
        this.mKeyAliases.setLayoutData(new GridData(768));
        this.mKeyAliasesLabel.setEnabled(false);
        this.mKeyAliases.setEnabled(false);
        new Composite(composite, 0).setLayoutData(gd = new GridData());
        gd.heightHint = 0;
        gd.widthHint = 0;
        this.mKeyPasswordLabel = new Label(composite, 0);
        this.mKeyPasswordLabel.setText("密码:");
        this.mKeyPassword = new Text(composite, 4196352);
        this.mKeyPassword.setLayoutData(new GridData(768));
        this.mKeyPassword.setEnabled(false);
        this.mKeyPasswordLabel.setEnabled(false);

        this.mKeystore.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                SignKeystore.setKeystorePath(Signer.this.mKeystore.getText().trim());
                Signer.this.onChange();
            }
        });

        this.mKeystorePassword.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                SignKeystore.setKeystorePassword(Signer.this.mKeystorePassword.getText());
                Signer.this.onChange();
            }
        });

        this.mKeyPassword.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                SignKeystore.setKeyPassword(Signer.this.mKeyPassword.getText());
            }
        });

        this.mKeyAliases.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                SignKeystore.setKeyAlias(Signer.this.mKeyAliases.getItem(Signer.this.mKeyAliases.getSelectionIndex()));
                Signer.this.mKeyPassword.setText("");
            }
        });

        Composite composite3 = new Composite(this.shell, 0);
        composite3.setLayoutData(new GridData(1800));
        GridLayout gl3 = new GridLayout(2, false);
        composite3.setLayout(gl3);
        this.mDestinationMsg = new Label(composite3, 0);
        this.mDestinationMsg.setText("请输入需要保存的路径:");
        GridData gd3;
        this.mDestinationMsg.setLayoutData(gd3 = new GridData(768));
        gd3.horizontalSpan = 3;

        this.mDestination = new Text(composite3, 2048);
        this.mDestination.setLayoutData(gd3 = new GridData(768));


        final Button browseButton3 = new Button(composite3, 8);
        browseButton3.setText("请选择");
        new Label(composite3, 0);
        new Label(composite3, 0);
        browseButton3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(browseButton3.getShell(), 8192);

                fileDialog.setFilterExtensions(new String[]{"*.apk", "*.apk"});
                fileDialog.setText("目标文件名");
                String saveLocation = fileDialog.open();
                if (saveLocation != null) {
                    Signer.this.mDestination.setText(saveLocation);
                }
                Signer.this.destValue = saveLocation;
            }
        });

        final Button OkButton = new Button(this.shell, 16777216);
        OkButton.setText("一键签名");
        OkButton.setLayoutData(new GridData(64));
        OkButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(final SelectionEvent e)
            {
                int style = 65600;
                final MessageBox messageBox = new MessageBox(Signer.this.shell, style);
                final SignApk sa = new SignApk();
                if ((SignKeystore.fileArray.size() == 0) ||
                        (SignKeystore.KEYSTORE_PATH.equals("")) || (SignKeystore.KEYSTORE_PWD.equals("")) ||
                        (SignKeystore.KEY_ALIAS.equals("")) || (SignKeystore.KEY_PWD.equals("")))
                {
                    Display.getDefault().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            messageBox.setText("注意");
                            messageBox.setMessage("请正确的输入!");
                            e.doit = (messageBox.open() == 64);
                        }
                    });
                    return;
                }
                OkButton.setEnabled(false);
                Thread t = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            int ret = sa.signed_apks();
                            if (ret == 0) {
                                SignApk.sign_by_command(Signer.this.shell, destValue, OkButton, e);
                            } else if (ret == 2) {
                                Display.getDefault().asyncExec(new Runnable()
                                {
                                    public void run()
                                    {
                                        messageBox.setText("提示");
                                        messageBox.setMessage(sa.getresult());
                                        e.doit = (messageBox.open() == 64);
                                        OkButton.setEnabled(true);
                                    }
                                });
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        });
    }

    private int onChange() {
        if ((this.mKeystore.getText().trim().length() > 0) && (this.mKeystorePassword.getText().length() > 0)) {
            try {
                this.mKeyAliases.removeAll();
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                FileInputStream fis = new FileInputStream(SignKeystore.KEYSTORE_PATH);
                keyStore.load(fis, SignKeystore.KEYSTORE_PWD.toCharArray());
                fis.close();
                Enumeration<String> aliases = keyStore.aliases();
                int count = 0;
                for (count = 0; aliases.hasMoreElements(); count++) {
                    String alias = (String) aliases.nextElement();
                    this.mKeyAliases.add(alias);
                }
                if (count != 0) {
                    this.mKeyAliasesLabel.setEnabled(true);
                    this.mKeyAliases.setEnabled(true);
                    this.mKeyPassword.setEnabled(true);
                    this.mKeyPasswordLabel.setEnabled(true);
                    this.mKeyAliases.select(0);
                    SignKeystore.setKeyAlias(this.mKeyAliases.getItem(0));
                    return 1;
                }
                this.mKeyAliasesLabel.setEnabled(false);
                this.mKeyAliases.setEnabled(false);
                this.mKeyPassword.setEnabled(false);
                this.mKeyPasswordLabel.setEnabled(false);


                this.mKeyPassword.setText("");
            } catch (KeyStoreException e) {
                System.out.println(e.getMessage());
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                System.out.println(e.getMessage());
            } catch (CertificateException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            this.mKeyAliasesLabel.setEnabled(false);
            this.mKeyAliases.setEnabled(false);
            this.mKeyPassword.setEnabled(false);
            this.mKeyPasswordLabel.setEnabled(false);
        }
        return -1;
    }

    public static String getProjectPath()
            throws UnsupportedEncodingException {
        URL url = Signer.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = URLDecoder.decode(url.getPath(), "utf-8");
        if (filePath.endsWith(".jar")) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }
        File file = new File(filePath);
        filePath = file.getAbsolutePath();

        filePath = filePath + File.separator + "confg";
        return filePath;
    }
}
