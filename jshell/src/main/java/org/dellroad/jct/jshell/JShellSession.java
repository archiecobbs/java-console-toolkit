
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

//import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.AbstractJctSession;
import org.dellroad.jct.core.JctShellSession;
import org.dellroad.jct.core.ShellRequest;
import org.jline.terminal.Terminal;

/**
 * Java Console Toolkit demonstration console.
 */
public class JShellSession extends AbstractJctSession implements JctShellSession {

    private final ShellRequest request;

    public JShellSession(ShellRequest request) {
        if (request == null)
            throw new IllegalArgumentException("null request");
        this.request = request;
    }

// JctShellSession

    @Override
    public ShellRequest getShellRequest() {
        return this.request;
    }

// AbstractJctSession

    @Override
    protected int doExecute() throws InterruptedException {
        return 1;
    }
}
