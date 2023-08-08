
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.util;

import java.io.FilterOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.function.Supplier;

import org.dellroad.jct.core.ConsoleSession;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Utility routines.
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
    }

    /**
     * Build a {@link Terminal} signal handler that {@link ConsoleSession#interrupt interrupt()}'s a {@link ConsoleSession}
     * when a {@link Terminal.Signal#INT} signal is received.
     *
     * <p>
     * This is needed when building a {@link Terminal} to make Control-C work in a shell session: pass the returned
     * handler to {@link TerminalBuilder#signalHandler TerminalBuilder.signalHandler()}.
     *
     * <p>
     * For example:
     * <blockquote><pre>
     *  Terminal terminal = TerminalBuilder.builder()
     *    .signalHandler(ConsoleUtil.interruptHandler(this::getSession, Terminal.SignalHandler.SIG_DFL))
     *    // other terminal config...
     *    .build();
     * </pre></blockquote>
     *
     * @param sessionGetter finds the session that should be interrupted; this may return null to demure
     * @param fallthroughHandler handler for signals other than {@link Terminal.Signal#INT},
     *  or when {@code sessionGetter} returns null; this may be null to do nothing
     * @return new signal handler
     * @throws IllegalArgumentException if {@code sessionGetter} is null
     */
    public static Terminal.SignalHandler interrruptHandler(Supplier<? extends ConsoleSession<?, ?>> sessionGetter,
      Terminal.SignalHandler fallthroughHandler) {
        if (sessionGetter == null)
            throw new IllegalArgumentException("null sessionGetter");
        return signal -> {
            if (signal.equals(Terminal.Signal.INT)) {
                final ConsoleSession<?, ?> session = sessionGetter.get();
                if (session != null) {
                    session.interrupt();
                    return;
                }
            }
            if (fallthroughHandler != null)
                fallthroughHandler.handle(signal);
        };
    }

    /**
     * Wrap an {@link PrintStream} in a new one that ignores {@link PrintStream#close close()}.
     *
     * @param stream original {@link PrintStream}
     * @return {@link PrintStream} that can't be closed
     * @throws IllegalArgumentException if {@code stream} is null
     */
    public static PrintStream unclosable(PrintStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("null stream");
        return new PrintStream(new FilterOutputStream(null)) {

            @Override
            public PrintStream append(char c) {
                stream.append(c);
                return this;
            }

            @Override
            public PrintStream append(CharSequence csq) {
                stream.append(csq);
                return this;
            }

            @Override
            public PrintStream append(CharSequence csq, int start, int end) {
                stream.append(csq, start, end);
                return this;
            }

            @Override
            public boolean checkError() {
                return stream.checkError();
            }

            @Override
            public void close() {
                // IGNORE
            }

            @Override
            public void flush() {
                stream.flush();
            }

            @Override
            public PrintStream format(Locale l, String format, Object... args) {
                stream.format(l, format, args);
                return this;
            }

            @Override
            public PrintStream format(String format, Object... args) {
                stream.format(format, args);
                return this;
            }

            @Override
            public void print(boolean b) {
                stream.print(b);
            }

            @Override
            public void print(char c) {
                stream.print(c);
            }

            @Override
            public void print(char[] s) {
                stream.print(s);
            }

            @Override
            public void print(double d) {
                stream.print(d);
            }

            @Override
            public void print(float f) {
                stream.print(f);
            }

            @Override
            public void print(int i) {
                stream.print(i);
            }

            @Override
            public void print(long l) {
                stream.print(l);
            }

            @Override
            public void print(Object obj) {
                stream.print(obj);
            }

            @Override
            public void print(String s) {
                stream.print(s);
            }

            @Override
            public PrintStream printf(Locale l, String format, Object... args) {
                stream.printf(l, format, args);
                return this;
            }

            @Override
            public PrintStream printf(String format, Object... args) {
                stream.printf(format, args);
                return this;
            }

            @Override
            public void println() {
                stream.println();
            }

            @Override
            public void println(boolean x) {
                stream.println(x);
            }

            @Override
            public void println(char x) {
                stream.println(x);
            }

            @Override
            public void println(char[] x) {
                stream.println(x);
            }

            @Override
            public void println(double x) {
                stream.println(x);
            }

            @Override
            public void println(float x) {
                stream.println(x);
            }

            @Override
            public void println(int x) {
                stream.println(x);
            }

            @Override
            public void println(long x) {
                stream.println(x);
            }

            @Override
            public void println(Object x) {
                stream.println(x);
            }

            @Override
            public void println(String x) {
                stream.println(x);
            }

            @Override
            public void write(byte[] buf, int off, int len) {
                stream.write(buf, off, len);
            }

            @Override
            public void write(int b) {
                stream.write(b);
            }
        };
    }
}
