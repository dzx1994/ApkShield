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

class StringItem {
    public String data;
    public int dataOffset;
    public int index;

    public StringItem(String data) {
        this.data = data;
    }

    public StringItem() {
    }

    public boolean equals(Object obj) {
        StringItem b = (StringItem)obj;
        return b.data.equals(this.data);
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    public String toString() {
        return String.format("S%04d %s", new Object[]{Integer.valueOf(this.index), this.data});
    }
}
