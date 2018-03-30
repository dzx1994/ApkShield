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

/**
 * visitor to visit an axml
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public abstract class AxmlVisitor {
    public static final int TYPE_FIRST_INT = 16;
    public static final int TYPE_INT_BOOLEAN = 18;
    public static final int TYPE_INT_HEX = 17;
    public static final int TYPE_REFERENCE = 1;
    public static final int TYPE_STRING = 3;
    protected AxmlVisitor av;

    public AxmlVisitor(AxmlVisitor av) {
        this.av = av;
    }

    public AxmlVisitor.NodeVisitor first(String ns, String name) {
        return this.av != null?this.av.first(ns, name):null;
    }

    public void ns(String prefix, String uri, int ln) {
        if(this.av != null) {
            this.av.ns(prefix, uri, ln);
        }

    }

    public void end() {
        if(this.av != null) {
            this.av.end();
        }

    }

    public abstract static class NodeVisitor {
        protected AxmlVisitor.NodeVisitor nv;

        public NodeVisitor(AxmlVisitor.NodeVisitor nv) {
            this.nv = nv;
        }

        public void attr(String ns, String name, int resourceId, int type, Object obj) {
            if(this.nv != null) {
                this.nv.attr(ns, name, resourceId, type, obj);
            }

        }

        public AxmlVisitor.NodeVisitor child(String ns, String name) {
            return this.nv != null?this.nv.child(ns, name):null;
        }

        public void text(int lineNumber, String value) {
            if(this.nv != null) {
                this.nv.text(lineNumber, value);
            }

        }

        public void line(int ln) {
            if(this.nv != null) {
                this.nv.line(ln);
            }

        }

        public void end() {
            if(this.nv != null) {
                this.nv.end();
            }

        }
    }
}
