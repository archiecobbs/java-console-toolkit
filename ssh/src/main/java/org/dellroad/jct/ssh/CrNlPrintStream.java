
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A {@link PrintStream} that always uses CR-LF for line endings.
 */
class CrNlPrintStream extends PrintStream {

    private static final char[] CRNL = new char[] { '\r', '\n' };

    CrNlPrintStream(OutputStream out, String charset) throws UnsupportedEncodingException {
        super(out, true, charset);
    }

    @Override
    public void println() {
        this.print(CRNL);
        this.flush();
    }

    public static PrintStream of(OutputStream out) {
        return CrNlPrintStream.of(out, StandardCharsets.UTF_8);
    }

    public static PrintStream of(OutputStream out, Charset charset) {
        if (out == null)
            throw new IllegalArgumentException("null out");
        if (charset == null)
            throw new IllegalArgumentException("null charset");
        try {
            return new CrNlPrintStream(out, charset.name());        // using JDK 8 compatible constructors
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("unexpected error", e);
        }
    }

    // These are all required to workaround https://bugs.openjdk.org/browse/JDK-8307863
    public void println(boolean x) {
        this.print(x);
        this.println();
    }
    public void println(char x) {
        this.print(x);
        this.println();
    }
    public void println(int x) {
        this.print(x);
        this.println();
    }
    public void println(long x) {
        this.print(x);
        this.println();
    }
    public void println(float x) {
        this.print(x);
        this.println();
    }
    public void println(double x) {
        this.print(x);
        this.println();
    }
    public void println(char[] x) {
        this.print(x);
        this.println();
    }
    public void println(String x) {
        this.print(x);
        this.println();
    }
    public void println(Object x) {
        this.print(x);
        this.println();
    }
}
