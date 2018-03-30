package pxb.android.axml;

import com.googlecode.dex2jar.reader.io.DataOut;
import com.googlecode.dex2jar.reader.io.LeDataOut;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.StringItem;
import pxb.android.axml.StringItems;
import pxb.android.axml.AxmlVisitor.NodeVisitor;

public class AxmlWriter extends AxmlVisitor {
    private AxmlWriter.NodeImpl first;
    private Set<AxmlWriter.Ns> nses = new HashSet();
    private List<StringItem> otherString = new ArrayList();
    private Map<Integer, StringItem> resourceId2Str = new HashMap();
    private List<Integer> resourceIds = new ArrayList();
    private List<StringItem> resourceString = new ArrayList();
    private StringItems stringItems = new StringItems();

    public AxmlWriter() {
        super((AxmlVisitor)null);
    }

    public void end() {
    }

    public NodeVisitor first(String ns, String name) {
        if(this.first != null) {
            throw new RuntimeException();
        } else {
            this.first = new AxmlWriter.NodeImpl(ns, name);
            return this.first;
        }
    }

    public void ns(String prefix, String uri, int ln) {
        this.nses.add(new AxmlWriter.Ns(new StringItem(prefix), new StringItem(uri), ln));
    }

    private int prepare() throws IOException {
        AxmlWriter.Ns size;
        for(Iterator stringSize = this.nses.iterator(); stringSize.hasNext(); size.uri = this.update(size.uri)) {
            size = (AxmlWriter.Ns)stringSize.next();
            size.prefix = this.update(size.prefix);
        }

        int size1 = this.nses.size() * 24 * 2;
        size1 += this.first.prepare(this);
        this.stringItems.addAll(this.resourceString);
        this.resourceString = null;
        this.stringItems.addAll(this.otherString);
        this.otherString = null;
        this.stringItems.prepare();
        int stringSize1 = this.stringItems.getSize();
        if(stringSize1 % 4 != 0) {
            stringSize1 += 4 - stringSize1 % 4;
        }

        size1 += 8 + stringSize1;
        size1 += 8 + this.resourceIds.size() * 4;
        return size1;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LeDataOut out = new LeDataOut(os);
        int size = this.prepare();
        out.writeInt(524291);
        out.writeInt(size + 8);
        int stringSize = this.stringItems.getSize();
        int padding = 0;
        if(stringSize % 4 != 0) {
            padding = 4 - stringSize % 4;
        }

        out.writeInt(1835009);
        out.writeInt(stringSize + padding + 8);
        this.stringItems.write(out);
        out.writeBytes(new byte[padding]);
        out.writeInt(524672);
        out.writeInt(8 + this.resourceIds.size() * 4);
        Iterator ns = this.resourceIds.iterator();

        while(ns.hasNext()) {
            Integer stack = (Integer)ns.next();
            out.writeInt(stack.intValue());
        }

        Stack stack1 = new Stack();
        Iterator var8 = this.nses.iterator();

        AxmlWriter.Ns ns1;
        while(var8.hasNext()) {
            ns1 = (AxmlWriter.Ns)var8.next();
            stack1.push(ns1);
            out.writeInt(1048832);
            out.writeInt(24);
            out.writeInt(-1);
            out.writeInt(-1);
            out.writeInt(ns1.prefix.index);
            out.writeInt(ns1.uri.index);
        }

        this.first.write(out);

        while(stack1.size() > 0) {
            ns1 = (AxmlWriter.Ns)stack1.pop();
            out.writeInt(1048833);
            out.writeInt(24);
            out.writeInt(ns1.ln);
            out.writeInt(-1);
            out.writeInt(ns1.prefix.index);
            out.writeInt(ns1.uri.index);
        }

        return os.toByteArray();
    }

    StringItem update(StringItem item) {
        if(item == null) {
            return null;
        } else {
            int i = this.otherString.indexOf(item);
            if(i < 0) {
                StringItem copy = new StringItem(item.data);
                this.otherString.add(copy);
                return copy;
            } else {
                return (StringItem)this.otherString.get(i);
            }
        }
    }

    StringItem updateWithResourceId(StringItem name, int resourceId) {
        StringItem item = (StringItem)this.resourceId2Str.get(Integer.valueOf(resourceId));
        if(item != null) {
            return item;
        } else {
            StringItem copy = new StringItem(name.data);
            this.resourceIds.add(Integer.valueOf(resourceId));
            this.resourceString.add(copy);
            this.resourceId2Str.put(Integer.valueOf(resourceId), copy);
            return copy;
        }
    }

    static class Attr {
        public StringItem name;
        public StringItem ns;
        public int resourceId;
        public int type;
        public Object value;

        public Attr(StringItem ns, StringItem name, int resourceId, int type, Object value) {
            this.ns = ns;
            this.name = name;
            this.resourceId = resourceId;
            this.type = type;
            this.value = value;
        }

        public void prepare(AxmlWriter axmlWriter) {
            this.ns = axmlWriter.update(this.ns);
            if(this.name != null) {
                if(this.resourceId != -1) {
                    this.name = axmlWriter.updateWithResourceId(this.name, this.resourceId);
                } else {
                    this.name = axmlWriter.update(this.name);
                }
            }

            if(this.value instanceof StringItem) {
                this.value = axmlWriter.update((StringItem)this.value);
            }

        }
    }

    static class NodeImpl extends NodeVisitor {
        private Map<String, AxmlWriter.Attr> attrs = new HashMap();
        private List<AxmlWriter.NodeImpl> children = new ArrayList();
        private int line;
        private StringItem name;
        private StringItem ns;
        private StringItem text;
        private int textLineNumber;

        public NodeImpl(String ns, String name) {
            super((NodeVisitor)null);
            this.ns = ns == null?null:new StringItem(ns);
            this.name = name == null?null:new StringItem(name);
        }

        public void attr(String ns, String name, int resourceId, int type, Object value) {
            if(name == null) {
                throw new RuntimeException("name can\'t be null");
            } else {
                this.attrs.put((ns == null?".*&&":ns) + "..." + name, new AxmlWriter.Attr(ns == null?null:new StringItem(ns), new StringItem(name), resourceId, type, type == 3?new StringItem((String)value):value));
            }
        }

        public NodeVisitor child(String ns, String name) {
            AxmlWriter.NodeImpl child = new AxmlWriter.NodeImpl(ns, name);
            this.children.add(child);
            return child;
        }

        public void end() {
        }

        public void line(int ln) {
            this.line = ln;
        }

        public int prepare(AxmlWriter axmlWriter) {
            this.ns = axmlWriter.update(this.ns);
            this.name = axmlWriter.update(this.name);
            Iterator child = this.attrs.values().iterator();

            while(child.hasNext()) {
                AxmlWriter.Attr size = (AxmlWriter.Attr)child.next();
                size.prepare(axmlWriter);
            }

            this.text = axmlWriter.update(this.text);
            int size1 = 60 + this.attrs.size() * 20;

            AxmlWriter.NodeImpl child1;
            for(Iterator var4 = this.children.iterator(); var4.hasNext(); size1 += child1.prepare(axmlWriter)) {
                child1 = (AxmlWriter.NodeImpl)var4.next();
            }

            if(this.text != null) {
                size1 += 28;
            }

            return size1;
        }

        public void text(int ln, String value) {
            this.text = new StringItem(value);
            this.textLineNumber = ln;
        }

        void write(DataOut out) throws IOException {
            out.writeInt(1048834);
            out.writeInt(36 + this.attrs.size() * 20);
            out.writeInt(this.line);
            out.writeInt(-1);
            out.writeInt(this.ns != null?this.ns.index:-1);
            out.writeInt(this.name.index);
            out.writeInt(1310740);
            out.writeShort(this.attrs.size());
            out.writeShort(0);
            out.writeShort(0);
            out.writeShort(0);
            Iterator var3 = this.attrs.values().iterator();

            while(var3.hasNext()) {
                AxmlWriter.Attr child = (AxmlWriter.Attr)var3.next();
                out.writeInt(child.ns == null?-1:child.ns.index);
                out.writeInt(child.name.index);
                out.writeInt(child.value instanceof StringItem?((StringItem)child.value).index:-1);
                out.writeInt(child.type << 24 | 8);
                if(child.value instanceof StringItem) {
                    out.writeInt(((StringItem)child.value).index);
                } else {
                    out.writeInt(((Integer)child.value).intValue());
                }
            }

            if(this.text != null) {
                out.writeInt(1048836);
                out.writeInt(28);
                out.writeInt(this.textLineNumber);
                out.writeInt(-1);
                out.writeInt(this.text.index);
                out.writeInt(8);
                out.writeInt(0);
            }

            var3 = this.children.iterator();

            while(var3.hasNext()) {
                AxmlWriter.NodeImpl child1 = (AxmlWriter.NodeImpl)var3.next();
                child1.write(out);
            }

            out.writeInt(1048835);
            out.writeInt(24);
            out.writeInt(-1);
            out.writeInt(-1);
            out.writeInt(this.ns != null?this.ns.index:-1);
            out.writeInt(this.name.index);
        }
    }

    static class Ns {
        int ln;
        StringItem prefix;
        StringItem uri;

        public Ns(StringItem prefix, StringItem uri, int ln) {
            this.prefix = prefix;
            this.uri = uri;
            this.ln = ln;
        }

        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            } else if(obj == null) {
                return false;
            } else if(this.getClass() != obj.getClass()) {
                return false;
            } else {
                AxmlWriter.Ns other = (AxmlWriter.Ns)obj;
                if(this.prefix == null) {
                    if(other.prefix != null) {
                        return false;
                    }
                } else if(!this.prefix.equals(other.prefix)) {
                    return false;
                }

                if(this.uri == null) {
                    if(other.uri != null) {
                        return false;
                    }
                } else if(!this.uri.equals(other.uri)) {
                    return false;
                }

                return true;
            }
        }

        public int hashCode() {
            boolean prime = true;
            byte result = 1;
            int result1 = 31 * result + (this.prefix == null?0:this.prefix.hashCode());
            result1 = 31 * result1 + (this.uri == null?0:this.uri.hashCode());
            return result1;
        }
    }
}