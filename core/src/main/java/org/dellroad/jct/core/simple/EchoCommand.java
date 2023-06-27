
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.Date;
import java.util.List;

import org.dellroad.jct.core.JctSession;

/**
 * A simple "echo" command that can be used with a {@link SimpleConsole}.
 */
public class EchoCommand extends AbstractSimpleCommand {

    public EchoCommand() {
        super("[arg ...]", "Echoes command line arguments.", "Prints the command line arguments separated by spaces.");
    }

    @Override
    public boolean execute(JctSession session, String name, List<String> args) throws InterruptedException {
        boolean first = true;
        for (String arg : args) {
            if (first)
                first = false;
            else
                session.getOutputStream().print(' ');
            session.getOutputStream().print(arg);
        }
        session.getOutputStream().println();
        return true;
    }
}
