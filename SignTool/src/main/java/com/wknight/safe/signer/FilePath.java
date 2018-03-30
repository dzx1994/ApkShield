package com.wknight.safe.signer;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;

import java.util.ArrayList;

public class FilePath {
    private String src_files;
    private String dest_files;
    private ArrayList<String> srcPath;
    private ArrayList<String> destPath;

    public FilePath()
    {
        this.src_files = new String("");
        this.dest_files = new String("");

        this.destPath = new ArrayList();
        this.srcPath = new ArrayList();
    }

    public String getSrcFiles()
    {
        return this.src_files;
    }

    public String getDestFiles()
    {
        return this.dest_files;
    }

    public ArrayList<String> getFileName()
    {
        return this.srcPath;
    }

    public ArrayList<String> getDestFilePath()
    {
        return this.destPath;
    }

    public void getMuitiFilePath(Button browseButton)
    {
        FileDialog fileDialog = new FileDialog(browseButton.getShell(), 4098);
        fileDialog.setText("请选择文件");

        String fileName = fileDialog.open();

        int file_Index = fileName.toString().lastIndexOf("\\");

        String[] fn_array = fileDialog.getFileNames();
        for (int i = 0; i < fn_array.length; i++)
        {
            String fn_dest = fileName.toString().substring(0, file_Index) + "\\" + fn_array[i];
            this.srcPath.add(fn_dest);
        }
        String append_str = new String("_signed.apk");
        String sep_str = new String("|");
        int fn_len = fn_array.length;
        int dotIndex = 0;
        for (String fn : this.srcPath)
        {
            this.src_files += fn;
            if (fn_len != 1) {
                this.src_files += sep_str;
            }
            if (fn.toString().lastIndexOf(".") != -1) {
                dotIndex = fn.toString().lastIndexOf(".");
            }
            String dest = fn.toString().substring(0, dotIndex) + append_str;
            this.destPath.add(dest);

            this.dest_files += fn.toString().substring(0, dotIndex);
            this.dest_files += append_str;
            if (fn_len != 1) {
                this.dest_files += sep_str;
            }
            fn_len--;
        }
    }
}
