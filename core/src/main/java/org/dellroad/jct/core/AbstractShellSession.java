
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;

import org.dellroad.jct.core.util.CrNlPrintStream;
import org.jline.terminal.Terminal;

/**
 * Support superclass for {@link ShellSession} implementations.
 */
public abstract class AbstractShellSession extends AbstractConsoleSession<Shell, ShellRequest> implements ShellSession {

    protected final InputStream in;
    protected final PrintStream out;

    /**
     * This will be non-null if {@link #setExitValue} has been invoked.
     */
    protected volatile Integer exitValue;

    /**
     * Constructor.
     *
     * @param owner session owner
     * @param request associated request
     * @throws IllegalArgumentException if either parameter is null
     */
    protected AbstractShellSession(Shell owner, ShellRequest request) {
        super(owner, request);
        final Terminal terminal = this.request.getTerminal();
        this.in = this.buildInputStream(terminal);
        this.out = this.buildOutputStream(terminal);
    }

    /**
     * Get the {@link InputStream} to use for input from the given {@link Terminal}.
     *
     * @param terminal terminal for shell
     * @return corresponding input stream
     * @throws IllegalArgumentException if {@code terminal} is null
     */
    protected InputStream buildInputStream(Terminal terminal) {
        return terminal.input();
    }

    /**
     * Get the {@link PrintStream} to use for output from the given {@link Terminal}.
     *
     * @param terminal terminal for shell
     * @return corresponding output stream
     * @throws IllegalArgumentException if {@code terminal} is null
     */
    protected PrintStream buildOutputStream(Terminal terminal) {
        return CrNlPrintStream.of(terminal);
    }

// ConsoleSession

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public PrintStream getOutputStream() {
        return this.out;
    }

    @Override
    public boolean setExitValue(int exitValue) {
        this.exitValue = exitValue;
        return true;
    }

    @Override
    public int getExitValue() {
        return this.exitValue != null ? this.exitValue : 0;
    }
}
