
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import org.dellroad.jct.core.AbstractActivity;
import org.dellroad.jct.core.JctShellSession;
import org.jline.terminal.Terminal;

/**
 * Java Console Toolkit demonstration console.
 */
public class JShellSession extends AbstractActivity implements JctShellSession {

    private final Terminal terminal;

    private volatile int exitValue;

    public JShellSession(Terminal terminal) {
        if (terminal == null)
            throw new IllegalArgumentException("null terminal");
        this.terminal = terminal;
    }

// JctShellSession

    @Override
    public Terminal getTerminal() {
        return this.terminal;
    }

// AbstractActivity

    @Override
    protected void handleException(Throwable t) {
    }

    @Override
    protected void internalClose() {
    }

    @Override
    protected void performActivity() throws Exception {
    }

    @Override
    public int getExitValue() {
        return this.exitValue;
    }
}
