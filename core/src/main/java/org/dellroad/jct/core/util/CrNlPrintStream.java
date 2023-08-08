
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jline.terminal.Terminal;

/**
 * A {@link PrintStream} that ends every line with a CR-NL sequence.
 */
public class CrNlPrintStream extends PrintStream {

    private static final char[] CRNL = new char[] { '\r', '\n' };

    public CrNlPrintStream(OutputStream out, boolean autoFlush, String charset) throws UnsupportedEncodingException {
        super(out, autoFlush, charset);
    }

    @Override
    public void println() {
        this.print(CRNL);
        this.flush();
    }

    /**
     * Create an instance using UTF-8 encoding.
     *
     * @param out underlying output stream
     * @return new stream
     * @throws IllegalArgumentException if {@code out} is null
     */
    public static CrNlPrintStream of(OutputStream out) {
        return CrNlPrintStream.of(out, StandardCharsets.UTF_8);
    }

    /**
     * Create an instance using the specified encoding.
     *
     * @param out underlying output stream
     * @param charset character encoding
     * @return new stream
     * @throws IllegalArgumentException if either parameter is null
     */
    public static CrNlPrintStream of(OutputStream out, Charset charset) {
        if (out == null)
            throw new IllegalArgumentException("null out");
        if (charset == null)
            throw new IllegalArgumentException("null charset");
        try {
            return new CrNlPrintStream(out, true, charset.name());        // using JDK 8 compatible constructors
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("unexpected error", e);
        }
    }

    /**
     * Create an instance appropriate for the given {@link Terminal}'s output.
     *
     * @param terminal underlying terminal
     * @return new stream
     * @throws IllegalArgumentException if {@code terminal} is null
     */
    public static CrNlPrintStream of(Terminal terminal) {
        if (terminal == null)
            throw new IllegalArgumentException("null terminal");
        return CrNlPrintStream.of(terminal.output(), terminal.encoding());
    }

// These are required to workaround this JDK bug: https://bugs.openjdk.org/browse/JDK-8307863

    // CHECKSTYLE OFF: LeftCurlyCheck
    // CHECKSTYLE OFF: OneStatementPerLine
    @Override public void println(boolean   x) { this.print(x); this.println(); }
    @Override public void println(char      x) { this.print(x); this.println(); }
    @Override public void println(int       x) { this.print(x); this.println(); }
    @Override public void println(long      x) { this.print(x); this.println(); }
    @Override public void println(float     x) { this.print(x); this.println(); }
    @Override public void println(double    x) { this.print(x); this.println(); }
    @Override public void println(char[]    x) { this.print(x); this.println(); }
    @Override public void println(String    x) { this.print(x); this.println(); }
    @Override public void println(Object    x) { this.print(x); this.println(); }
    // CHECKSTYLE ON: OneStatementPerLine
    // CHECKSTYLE ON: LeftCurlyCheck
}
