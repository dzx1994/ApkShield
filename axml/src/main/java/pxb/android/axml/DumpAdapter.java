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

import java.util.HashMap;
import java.util.Map;

/**
 * dump axml to stdout
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */

public class DumpAdapter extends AxmlVisitor {
    private Map<String, String> nses = new HashMap();

    public DumpAdapter(AxmlVisitor av) {
        super(av);
    }

    public void ns(String prefix, String uri, int ln) {
        System.out.println(prefix + "=" + uri);
        this.nses.put(uri, prefix);
        super.ns(prefix, uri, ln);
    }

    public NodeVisitor first(String ns, String name) {
        System.out.print("<");
        if(ns != null) {
            System.out.println((String)this.nses.get(ns) + ":");
        }

        System.out.println(name);
        NodeVisitor nv = super.first(ns, name);
        if(nv != null) {
            DumpAdapter.DumpNodeAdapter x = new DumpAdapter.DumpNodeAdapter(nv, 1, this.nses);
            return x;
        } else {
            return null;
        }
    }

    public void end() {
        super.end();
    }

    public static class DumpNodeAdapter extends NodeVisitor {
        protected int deep;
        protected Map<String, String> nses;

        public void text(int ln, String value) {
            for(int i = 0; i < this.deep + 1; ++i) {
                System.out.print("  ");
            }

            System.out.println(value);
            super.text(ln, value);
        }

        public DumpNodeAdapter(NodeVisitor nv) {
            super(nv);
            this.deep = 0;
            this.nses = null;
        }

        public DumpNodeAdapter(NodeVisitor nv, int x, Map<String, String> nses) {
            super(nv);
            this.deep = x;
            this.nses = nses;
        }

        protected String getPrefix(String uri) {
            if(this.nses != null) {
                String prefix = (String)this.nses.get(uri);
                if(prefix != null) {
                    return prefix;
                }
            }

            return uri;
        }

        public void attr(String ns, String name, int resourceId, int type, Object obj) {
            for(int i = 0; i < this.deep; ++i) {
                System.out.print("  ");
            }

            if(ns != null) {
                System.out.print(String.format("%s:", new Object[]{this.getPrefix(ns)}));
            }

            System.out.print(name);
            if(resourceId != -1) {
                System.out.print(String.format("(%08x)", new Object[]{Integer.valueOf(resourceId)}));
            }

            if(obj instanceof String) {
                System.out.print(String.format("=[%08x]\"%s\"", new Object[]{Integer.valueOf(type), obj}));
            } else {
                System.out.print(String.format("=[%08x]%08x", new Object[]{Integer.valueOf(type), obj}));
            }

            System.out.println();
            super.attr(ns, name, resourceId, type, obj);
        }

        public NodeVisitor child(String ns, String name) {
            for(int nv = 0; nv < this.deep; ++nv) {
                System.out.print("  ");
            }

            System.out.print("<");
            if(ns != null) {
                System.out.println(this.getPrefix(ns) + ":");
            }

            System.out.println(name);
            NodeVisitor var4 = super.child(ns, name);
            return var4 != null?new DumpAdapter.DumpNodeAdapter(var4, this.deep + 1, this.nses):null;
        }
    }
}
