/*
 * Copyright (c) 2009-2013 Panxiaobo
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
package pxb.android.axml;

import com.googlecode.dex2jar.reader.io.DataIn;
import com.googlecode.dex2jar.reader.io.DataOut;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.StringItem;

class StringItems extends ArrayList<StringItem> {
    byte[] stringData;
    static final int UTF8_FLAG = 0x00000100;

    StringItems() {
    }

    public static String[] read(ByteBuffer in) throws IOException {
        int trunkOffset = in.position() - 8;
        int stringCount = in.getInt();
        int styleOffsetCount = in.getInt();
        int flags = in.getInt();
        int stringDataOffset = in.getInt();
        int stylesOffset = in.getInt();
        int offsets[] = new int[stringCount];
        String strings[] = new String[stringCount];
        for (int i = 0; i < stringCount; i++) {
            offsets[i] = in.getInt();
        }

        if (stylesOffset != 0) {
            System.err.println("ignore style offset at 0x" + Integer.toHexString(trunkOffset));
        }
        int base = trunkOffset + stringDataOffset;
        for (int i = 0; i < offsets.length; i++) {
            in.position(base + offsets[i]);
            String s;

            if (0 != (flags & UTF8_FLAG)) {
                u8length(in); // ignored
                int u8len = u8length(in);
                int start = in.position();
                int blength = u8len;
                while (in.get(start + blength) != 0) {
                    blength++;
                }
                s = new String(in.array(), start, blength, "UTF-8");
            } else {
                int length = u16length(in);
                s = new String(in.array(), in.position(), length * 2, "UTF-16LE");
            }
            strings[i] = s;
        }
        return strings;
    }

    static int u16length(ByteBuffer in) {
        int length = in.getShort() & 0xFFFF;
        if (length > 0x7FFF) {
            length = ((length & 0x7FFF) << 8) | (in.getShort() & 0xFFFF);
        }
        return length;
    }

    static int u8length(ByteBuffer in) {
        int len = in.get() & 0xFF;
        if ((len & 0x80) != 0) {
            len = ((len & 0x7F) << 8) | (in.get() & 0xFF);
        }
        return len;
    }

    public void read(DataIn in, int size, String applicationName) throws Exception {
        int trunkOffset = in.getCurrentPosition() - 4;
        int stringCount = in.readIntx();
        AxmlReader.mStringCountOffset = in.getCurrentPosition() - 4;
        AxmlReader.mStringCount = stringCount + 1;
        int styleOffsetCount = in.readIntx();
        int flags = in.readIntx();
        int stringDataOffset = in.readIntx();
        AxmlReader.mStringDataOffsetOffset = in.getCurrentPosition() - 4;
        AxmlReader.mStringDataOffset = stringDataOffset + 4;
        int stylesOffset = in.readIntx();

        for(int stringMap = 0; stringMap < stringCount; ++stringMap) {
            StringItem endOfStringData = new StringItem();
            endOfStringData.index = stringMap;
            endOfStringData.dataOffset = in.readIntx();
            this.add(endOfStringData);
        }

        AxmlReader.mStringAppNameOffsetOffset = in.getCurrentPosition();
        TreeMap var18 = new TreeMap();
        if(styleOffsetCount != 0) {
            throw new RuntimeException();
        } else {
            int var17 = stylesOffset == 0?size:stylesOffset;
            int base = in.getCurrentPosition();
            int applicationNameData;
            int stringSize;
            String var23;
            if((flags & 256) != 0) {
                for(applicationNameData = base; applicationNameData < var17; applicationNameData = in.getCurrentPosition()) {
                    stringSize = (int)in.readLeb128();
                    ByteArrayOutputStream item = new ByteArrayOutputStream(stringSize + 10);

                    for(int value = in.readByte(); value != 0; value = in.readByte()) {
                        item.write(value);
                    }

                    var23 = new String(item.toByteArray(), "UTF-8");
                    var18.put(Integer.valueOf(applicationNameData - base), var23);
                }
            } else {
                for(applicationNameData = base; applicationNameData < var17; applicationNameData = in.getCurrentPosition()) {
                    stringSize = in.readShortx();
                    byte[] var20 = in.readBytes(stringSize * 2);
                    in.skip(2);
                    var23 = new String(var20, "UTF-16LE");
                    var18.put(Integer.valueOf(applicationNameData - base), var23);
                }
            }

            AxmlReader.mStringAppNameOffset = in.getCurrentPosition() - base;
            ByteArrayOutputStream var21 = new ByteArrayOutputStream();
            if((flags & 256) != 0) {
                AxmlReader.writeSignedLeb128(var21, applicationName.length());
                var21.write(applicationName.getBytes("UTF-8"));
                var21.write(0);
            } else {
                short var22 = (short)applicationName.length();
                AxmlReader.writeShort(var21, var22);
                var21.write(applicationName.getBytes("UTF-16LE"));
                byte var19 = 0;
                AxmlReader.writeShort(var21, var19);
            }

            stringSize = AxmlReader.mStringChunkSize + 4 + var21.size();
            if(stringSize % 4 != 0) {
                int var25 = 4 - stringSize % 4;
                var21.write(new byte[var25]);
            }

            AxmlReader.mStringAppNameIndex = stringCount;
            AxmlReader.mStringAppNameDataOffset = in.getCurrentPosition() + 4;
            AxmlReader.mStringAppNameData = var21.toByteArray();
            Iterator var26 = this.iterator();

            while(var26.hasNext()) {
                StringItem var24 = (StringItem)var26.next();
                var24.data = (String)var18.get(Integer.valueOf(var24.dataOffset));
                if(AxmlReader.mStringNameIndex == -1 && var24.data != null && var24.data.equals("name")) {
                    AxmlReader.mStringNameIndex = var24.index;
                }
            }

        }
    }

    public void prepare() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        int offset = 0;
        baos.reset();
        HashMap map = new HashMap();
        Iterator var6 = this.iterator();

        while(var6.hasNext()) {
            StringItem item = (StringItem)var6.next();
            item.index = i++;
            String stringData = item.data;
            Integer of = (Integer)map.get(stringData);
            if(of != null) {
                item.dataOffset = of.intValue();
            } else {
                item.dataOffset = offset;
                map.put(stringData, Integer.valueOf(offset));
                int length = stringData.length();
                byte[] data = stringData.getBytes("UTF-16LE");
                baos.write(length);
                baos.write(length >> 8);
                baos.write(data);
                baos.write(0);
                baos.write(0);
                offset += 4 + data.length;
            }
        }

        this.stringData = baos.toByteArray();
    }

    public int getSize() {
        return 20 + this.size() * 4 + this.stringData.length + 0;
    }

    public void write(DataOut out) throws IOException {
        out.writeInt(this.size());
        out.writeInt(0);
        out.writeInt(0);
        out.writeInt(28 + this.size() * 4);
        out.writeInt(0);
        Iterator var3 = this.iterator();

        while(var3.hasNext()) {
            StringItem item = (StringItem)var3.next();
            out.writeInt(item.dataOffset);
        }

        out.writeBytes(this.stringData);
    }
}
