
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Support superclass for {@link JctShellSession} implementations.
 */
public abstract class AbstractJctShellSession extends AbstractJctSession implements JctShellSession {

    protected final ShellRequest request;
    protected final PrintStream out;

    /**
     * Constructor.
     *
     * @param console associated console
     * @param request associated shell request
     * @throws IllegalArgumentException if either parameter is null
     */
    protected AbstractJctShellSession(JctConsole console, ShellRequest request) {
        super(console);
        if (request == null)
            throw new IllegalArgumentException("null request");
        this.request = request;
        this.out = CrNlPrintStream.of(this.request.getTerminal());
    }

// JctShellSession

    @Override
    public InputStream getInputStream() {
        return this.request.getTerminal().input();
    }

    @Override
    public PrintStream getOutputStream() {
        return this.out;
    }

    @Override
    public ShellRequest getShellRequest() {
        return this.request;
    }
}
