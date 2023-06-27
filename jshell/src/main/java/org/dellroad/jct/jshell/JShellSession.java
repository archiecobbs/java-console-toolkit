
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

//import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.AbstractJctShellSession;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.ShellRequest;
//import org.jline.terminal.Terminal;

/**
 * Java Console Toolkit demonstration console.
 */
public class JShellSession extends AbstractJctShellSession {

    public JShellSession(JctConsole console, ShellRequest request) {
        super(console, request);
    }

// AbstractJctSession

    @Override
    protected boolean doExecute() throws InterruptedException {
        return false;
    }
}
