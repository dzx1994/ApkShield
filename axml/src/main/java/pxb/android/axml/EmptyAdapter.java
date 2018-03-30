package pxb.android.axml;


import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlVisitor.NodeVisitor;

public class EmptyAdapter extends AxmlVisitor {
    public EmptyAdapter() {
        super((AxmlVisitor)null);
    }

    public NodeVisitor first(String ns, String name) {
        return new EmptyAdapter.EmptyNode();
    }

    public static class EmptyNode extends NodeVisitor {
        public EmptyNode() {
            super((NodeVisitor)null);
        }

        public NodeVisitor child(String ns, String name) {
            return new EmptyAdapter.EmptyNode();
        }
    }
}