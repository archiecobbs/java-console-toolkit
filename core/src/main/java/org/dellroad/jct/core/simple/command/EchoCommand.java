
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import java.util.List;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.simple.AbstractSimpleCommand;

/**
 * A simple "echo" command.
 */
public class EchoCommand extends AbstractSimpleCommand {

    public EchoCommand() {
        super("[arg ...]", "Echoes command line arguments.", "Prints the command line arguments separated by spaces.");
    }

    @Override
    public int execute(ConsoleSession<?, ?> session, String name, List<String> args) throws InterruptedException {
        boolean first = true;
        for (String arg : args) {
            if (first)
                first = false;
            else
                session.getOutputStream().print(' ');
            session.getOutputStream().print(arg);
        }
        session.getOutputStream().println();
        return 0;
    }
}
