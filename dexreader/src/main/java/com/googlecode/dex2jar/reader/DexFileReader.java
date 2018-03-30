/*
 * Copyright (c) 2009-2011 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.reader;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.reader.io.BeArrayDataIn;
import com.googlecode.dex2jar.reader.io.DataIn;
import com.googlecode.dex2jar.reader.io.LeArrayDataIn;
import com.googlecode.dex2jar.util.Utf8Utils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 读取dex文件
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexFileReader {
    private static final byte[] DEX_FILE_MAGIC = new byte[] { 0x64, 0x65, 0x78 };
    private static final byte[] ODEX_FILE_MAGIC = new byte[] { 0x64, 0x65, 0x79 };
    private static final byte[] VERSION_035 = new byte[] { 0x30, 0x33, 0x35 };
    private static final byte[] VERSION_036 = new byte[] { 0x30, 0x33, 0x36 };

    /* default */static final int ENDIAN_CONSTANT = 0x12345678;

    private DataIn in;
    private int string_ids_off;
    private int string_ids_size;

    private boolean odex = false;
    private Set<String> strings_set = null;

    public Set<String> loadStrings()
    {
        if (strings_set == null)
        {
            strings_set = new HashSet<String>();
            for (int sid = 0; sid < this.string_ids_size; sid++)
                try{
                    String tmpString = getString(sid);
                    if (tmpString.startsWith("Landroid/")
                            || tmpString.startsWith("Ljava/")
                            || tmpString.startsWith("Lorg/")
                            || tmpString.startsWith("Lcom/")
                            || tmpString.length()==0
                            || !isUseful(tmpString)
                            )
                        continue;
                    strings_set.add(tmpString);
                }catch (Throwable e){
//                    System.out.println("get String error, sid:" + sid + " offset:" +string_ids_off+sid*4);
                    System.out.println("bad dex file, can not load all Strings");
                }

        }
        return strings_set;
    }

    public boolean isUseful(String str){
        Pattern pattern = Pattern.compile("^[a-zA-Z_][0-9a-zA-Z_]*");
        return pattern.matcher(str).matches();
    }

	public static byte[] readDex(byte[] data) throws IOException {
	    if ("de".equals(new String(data, 0, 2))) {// dex/y
            return data;
        } else if ("PK".equals(new String(data, 0, 2))) {// ZIP
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new ByteArrayInputStream(data));
                for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                    if (entry.getName().equals("classes.dex")) {
                        return IOUtils.toByteArray(zis);
                    }
                }
            } finally {
                IOUtils.closeQuietly(zis);
            }
        }
        throw new RuntimeException("the src file not a .dex, .odex or zip file");
    }

    public DexFileReader(DataIn in) {
        byte[] magic = in.readBytes(3);

        if (Arrays.equals(magic, DEX_FILE_MAGIC)) {
            //
        } else if (Arrays.equals(magic, ODEX_FILE_MAGIC)) {
            odex = true;
        } else {
            throw new DexException("not support magic.");
        }
        in.skip(1);// 0x0A
        byte[] version = in.readBytes(3);
        if (!Arrays.equals(version, VERSION_035) && !Arrays.equals(version, VERSION_036)) {
            throw new DexException("not support version.");
        }
        in.skip(1);// 0x00

        if (odex) {
            int base = in.readIntx();// odex_dexOffset
            in.skip(4);// odex_dexLength
            in = new OffsetedDataIn(in, base);
            in.skip(8);// skip head;
        }

        in.skip(4 + 20 + 4 + 4);

        int endian_tag = in.readUIntx();
        if (endian_tag != ENDIAN_CONSTANT) {
            throw new DexException("not support endian_tag");
        }

        this.in = in;
        in.skip(4 + 4 + 4);

        string_ids_size = in.readUIntx();
        string_ids_off = in.readUIntx();
    }

    private static final boolean isLittleEndian = true;

    static private DataIn opDataIn(byte[] data) {
        try {
            if (isLittleEndian) {
                return new LeArrayDataIn(readDex(data));
            } else {
                return new BeArrayDataIn(readDex(data));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new DexException(e);
        }
    }

    /**
     * 
     * @param data
     * 
     */
    public DexFileReader(byte[] data) {
        this(opDataIn(data));
    }

    public DexFileReader(InputStream in) throws IOException {
        this(IOUtils.toByteArray(in));
    }

    /**
     * 一个String id为4字节
     */
    String getString(int id) {
        if (id >= this.string_ids_size || id < 0)
            throw new IllegalArgumentException("Id out of bound");
        DataIn in = this.in;
        int idxOffset = this.string_ids_off + id * 4;
        in.pushMove(idxOffset);
        try {
            int offset = in.readIntx();
            in.pushMove(offset);
            try {
                int length = (int) in.readULeb128();
                ByteArrayOutputStream baos = new ByteArrayOutputStream(length*2);
                for (int b = in.readByte(); b != 0; b = in.readByte()) {
                    baos.write(b);
                }
                final byte[] bytes = baos.toByteArray();
                final String s = Utf8Utils.utf8BytesToString(bytes, 0, bytes.length);
                return s;
            } finally {
                in.pop();
            }
        } finally {
            in.pop();
        }
    }
}
