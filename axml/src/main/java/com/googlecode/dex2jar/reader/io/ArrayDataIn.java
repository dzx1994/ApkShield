/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.dex2jar.reader.io;

import com.googlecode.dex2jar.reader.io.DataIn;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Stack;

public abstract class ArrayDataIn extends ByteArrayInputStream implements DataIn {
    private Stack<Integer> stack = new Stack();

    public ArrayDataIn(byte[] data) {
        super(data);
    }

    public int readShortx() {
        return (short)this.readUShortx();
    }

    public int readIntx() {
        return this.readUIntx();
    }

    public int getCurrentPosition() {
        return super.pos;
    }

    public void move(int absOffset) {
        super.pos = absOffset;
    }

    public void pop() {
        super.pos = ((Integer)this.stack.pop()).intValue();
    }

    public void push() {
        this.stack.push(Integer.valueOf(super.pos));
    }

    public void pushMove(int absOffset) {
        this.push();
        this.move(absOffset);
    }

    public byte[] readBytes(int size) {
        byte[] data = new byte[size];

        try {
            super.read(data);
            return data;
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    public long readLeb128() {
        int bitpos = 0;
        long vln = 0L;

        int inp;
        do {
            inp = this.readUByte();
            vln |= (long)(inp & 127) << bitpos;
            bitpos += 7;
        } while((inp & 128) != 0);

        if((1L << bitpos - 1 & vln) != 0L) {
            vln -= 1L << bitpos;
        }

        return vln;
    }

    public long readULeb128() {
        long value = 0L;
        int count = 0;

        int b;
        for(b = this.readUByte(); (b & 128) != 0; b = this.readUByte()) {
            value |= (long)((b & 127) << count);
            count += 7;
        }

        value |= (long)((b & 127) << count);
        return value;
    }

    public void skip(int bytes) {
        super.skip((long)bytes);
    }

    public int readByte() {
        return (byte)this.readUByte();
    }

    public int readUByte() {
        if(super.pos >= super.count) {
            throw new RuntimeException("EOF");
        } else {
            return super.read();
        }
    }
}
