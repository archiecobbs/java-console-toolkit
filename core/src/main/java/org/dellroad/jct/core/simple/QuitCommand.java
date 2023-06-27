
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.List;

import org.dellroad.jct.core.JctSession;
import org.dellroad.jct.core.JctShellSession;

/**
 * A simple "quit" command that can be used with a {@link SimpleConsole}.
 */
public class QuitCommand extends AbstractSimpleCommand {

    public QuitCommand() {
        super(null, "Exit the shell console.", "Causes the shell console to exit.");
    }

    @Override
    public boolean execute(JctSession session, String name, List<String> args) throws InterruptedException {

        // Get console
        final SimpleConsole console = this.getSimpleConsole(session, name);
        if (console == null)
            return false;

        // Quit console
        if (!(session instanceof JctShellSession) || !console.quit((JctShellSession)session)) {
            session.getErrorStream().println("Error: unable to quit session");
            return false;
        }

        // Done
        return true;
    }
}
