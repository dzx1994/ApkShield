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
import com.googlecode.dex2jar.reader.io.LeArrayDataIn;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import pxb.android.axml.StringItem;
import pxb.android.axml.StringItems;

public class AxmlReader {
    static final int UTF8_FLAG = 256;
    static final int CHUNK_AXML_FILE = 524291;
    static final int CHUNK_RESOURCEIDS = 524672;
    static final int CHUNK_STRINGS = 1835009;
    static final int CHUNK_XML_END_NAMESPACE = 1048833;
    static final int CHUNK_XML_END_TAG = 1048835;
    static final int CHUNK_XML_START_NAMESPACE = 1048832;
    static final int CHUNK_XML_START_TAG = 1048834;
    static final int CHUNK_XML_TEXT = 1048836;
    private StringItems stringItems;
    private List<Integer> resourceIds;
    private DataIn in;
    public static int mFileSize = 0;
    public static int mFileSizeOffset = -1;
    public static int mStringChunkSize = 0;
    public static int mStringChunkSizeOffset = -1;
    public static int mStringCount = 0;
    public static int mStringCountOffset = -1;
    public static int mStringDataOffset = 0;
    public static int mStringDataOffsetOffset = -1;
    public static int mStringAppNameOffset = 0;
    public static int mStringAppNameOffsetOffset = -1;
    public static byte[] mStringAppNameData = null;
    public static int mStringAppNameDataOffset = -1;
    public static int mStringAppNameIndex = -1;
    public static int mApplicationAttrSize = 0;
    public static int mApplicationAttrSizeOffset = -1;
    public static int mApplicationAttrCount = 0;
    public static int mApplicationAttrCountOffset = -1;
    public static int mAppNameValueUnknown = 0;
    public static int mAppNameValueUnknownOffset = -1;
    public static int mAppNameValueType = 0;
    public static int mAppNameValueTypeOffset = -1;
    public static int mAppNameValueIndex = 0;
    public static int mAppNameValueIndexOffset = -1;
    public static byte[] mAttrAppNameData = null;
    public static int mAttrAppNameDataOffset = -1;
    public static int mStringNameIndex = -1;
    public static final int STRING_VALUE_TYPE = 50331656;

    public AxmlReader(byte[] data) {
        this((DataIn)(new LeArrayDataIn(data)));
    }

    public AxmlReader(DataIn in) {
        this.stringItems = new StringItems();
        this.resourceIds = new ArrayList();
        this.in = in;
    }

    public void preprocess(String applicationName) throws Exception {
        DataIn in = this.in;
        int appNameAttrExists = in.readIntx();
        if(appNameAttrExists != 524291) {
            throw new RuntimeException();
        } else {
            int fileSize = in.readIntx();
            mFileSizeOffset = in.getCurrentPosition() - 4;
            mFileSize = fileSize;
            boolean var26 = false;
            int stringAttrNameSpace = -1;
            boolean foundFirstApplicationTag = false;

            for(int p = in.getCurrentPosition(); p < fileSize; p = in.getCurrentPosition()) {
                int size;
                int type = in.readIntx();
                int sizeOffset = in.getCurrentPosition();
                size = in.readIntx();
                String name;
                int nameIdx;
                int nsIdx;
                int lineNumber;
                int count;
                int prefixIdx;
                int i;
                label120:
                switch(type) {
                    case 524672:
                        count = size / 4 - 2;
                        i = 0;

                        while(true) {
                            if(i >= count) {
                                break label120;
                            }

                            this.resourceIds.add(Integer.valueOf(in.readIntx()));
                            ++i;
                        }
                    case 1048832:
                        lineNumber = in.readIntx();
                        in.skip(4);
                        prefixIdx = in.readIntx();
                        nsIdx = in.readIntx();
                        stringAttrNameSpace = nsIdx;
                        break;
                    case 1048833:
                        in.skip(size - 8);
                        break;
                    case 1048834:
                        lineNumber = in.readIntx();
                        in.skip(4);
                        nsIdx = in.readIntx();
                        nameIdx = in.readIntx();
                        prefixIdx = in.readIntx();
                        name = ((StringItem)this.stringItems.get(nameIdx)).data;
                        String var10000;
                        if(nsIdx >= 0) {
                            var10000 = ((StringItem)this.stringItems.get(nsIdx)).data;
                        } else {
                            var10000 = null;
                        }

                        prefixIdx = in.getCurrentPosition();
                        count = in.readUShortx();
                        in.skip(6);
                        i = in.getCurrentPosition();
                        boolean isAttrNameEmpty = true;
                        boolean noNameAttrNameModified = false;
                        int greatestValueTypeFound = -1;

                        for(int appNameAttrData = 0; appNameAttrData < count; ++appNameAttrData) {
                            nsIdx = in.readIntx();
                            nameIdx = in.readIntx();
                            in.skip(4);
                            int aValueType = in.readIntx() >>> 24;
                            int aValue = in.readIntx();
                            if(aValueType > greatestValueTypeFound) {
                                greatestValueTypeFound = aValueType;
                            }

                            if(!foundFirstApplicationTag && name != null && name.equals("application") && greatestValueTypeFound <= 3 && aValueType < 3) {
                                i = in.getCurrentPosition();
                            }

                            String attrName = ((StringItem)this.stringItems.get(nameIdx)).data;
                            if(!foundFirstApplicationTag && name != null && attrName != null && name.equals("application") && attrName.equals("name")) {
                                var26 = true;
                                mAppNameValueUnknown = mStringAppNameIndex;
                                mAppNameValueUnknownOffset = in.getCurrentPosition() - 12 + 4 + mStringAppNameData.length;
                                mAppNameValueType = 50331656;
                                mAppNameValueTypeOffset = in.getCurrentPosition() - 8 + 4 + mStringAppNameData.length;
                                mAppNameValueIndex = mStringAppNameIndex;
                                mAppNameValueIndexOffset = in.getCurrentPosition() - 4 + 4 + mStringAppNameData.length;
                            }

                            if(!foundFirstApplicationTag && name != null && name.equals("application")) {
                                if(attrName != null && attrName.length() > 0) {
                                    isAttrNameEmpty = false;
                                }

                                if(isAttrNameEmpty && aValueType == 3 && !noNameAttrNameModified) {
                                    var26 = true;
                                    noNameAttrNameModified = true;
                                    mAppNameValueUnknown = mStringAppNameIndex;
                                    mAppNameValueUnknownOffset = in.getCurrentPosition() - 12 + 4 + mStringAppNameData.length;
                                    mAppNameValueType = 50331656;
                                    mAppNameValueTypeOffset = in.getCurrentPosition() - 8 + 4 + mStringAppNameData.length;
                                    mAppNameValueIndex = mStringAppNameIndex;
                                    mAppNameValueIndexOffset = in.getCurrentPosition() - 4 + 4 + mStringAppNameData.length;
                                }
                            }
                        }

                        if(!foundFirstApplicationTag && name != null && name.equals("application") && !var26) {
                            if(mStringNameIndex < 0) {
                                throw new Exception("not found name string");
                            }

                            if(stringAttrNameSpace < 0) {
                                stringAttrNameSpace = nsIdx;
                            }

                            ByteArrayOutputStream var27 = new ByteArrayOutputStream();
                            writeInt(var27, stringAttrNameSpace);
                            writeInt(var27, mStringNameIndex);
                            writeInt(var27, mStringAppNameIndex);
                            writeInt(var27, 50331656);
                            writeInt(var27, mStringAppNameIndex);
                            mAttrAppNameData = var27.toByteArray();
                            mAttrAppNameDataOffset = i + 4 + mStringAppNameData.length;
                            mApplicationAttrCount = count + 1;
                            mApplicationAttrCountOffset = prefixIdx + 4 + mStringAppNameData.length;
                            mApplicationAttrSize = size + mAttrAppNameData.length;
                            mApplicationAttrSizeOffset = sizeOffset + 4 + mStringAppNameData.length;
                        }

                        if(name != null && name.equals("application") && !foundFirstApplicationTag) {
                            foundFirstApplicationTag = true;
                        }
                        break;
                    case 1048835:
                        in.skip(size - 8);
                        break;
                    case 1048836:
                        lineNumber = in.readIntx();
                        in.skip(4);
                        nameIdx = in.readIntx();
                        in.skip(8);
                        name = ((StringItem)this.stringItems.get(nameIdx)).data;
                        break;
                    case 1835009:
                        mStringChunkSize = size;
                        mStringChunkSizeOffset = in.getCurrentPosition() - 4;
                        this.stringItems.read(in, size, applicationName);
                }

                in.move(p + size);
            }

            mFileSize += 4;
            mFileSize += mStringAppNameData.length;
            if(mAttrAppNameData != null) {
                mFileSize += mAttrAppNameData.length;
            }

            mStringChunkSize += 4;
            mStringChunkSize += mStringAppNameData.length;
        }
    }

    public static void writeSignedLeb128(ByteArrayOutputStream out, int value) {
        int remaining = value >> 7;
        boolean hasMore = true;

        for(int end = (value & -2147483648) == 0?0:-1; hasMore; remaining >>= 7) {
            hasMore = remaining != end || (remaining & 1) != (value >> 6 & 1);
            out.write((byte)(value & 127 | (hasMore?128:0)));
            value = remaining;
        }

    }

    public static void writeShort(ByteArrayOutputStream out, short value) throws Exception {
        byte[] byteVal = new byte[]{(byte)(value & 255), (byte)(value >> 8 & 255)};
        out.write(byteVal);
    }

    public static void writeInt(ByteArrayOutputStream out, int value) throws Exception {
        byte[] byteVal = new byte[]{(byte)(value & 255), (byte)(value >> 8 & 255), (byte)(value >> 16 & 255), (byte)(value >> 24 & 255)};
        out.write(byteVal);
    }
}
