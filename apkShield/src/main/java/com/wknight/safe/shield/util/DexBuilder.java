package com.wknight.safe.shield.util;

import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DexBuilder{
    private String out;
    private List<String> input;
    private SmaliOptions options;

    public DexBuilder(String in, String out) {
        this.input = new ArrayList<String>();
        this.input.add(in);
        this.out = out;
        options = getOptions();
    }

    public void buildDex() {
        try {
            Smali.assemble(options, input);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected SmaliOptions getOptions() {
        SmaliOptions options = new SmaliOptions();
        options.jobs = Runtime.getRuntime().availableProcessors();
        options.apiLevel = 15;
        options.outputDexFile = out;
        options.allowOdexOpcodes = false;
        options.verboseErrors = false;
        return options;
    }
}
