
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import java.util.List;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.simple.AbstractSimpleCommand;

/**
 * A simple "exit" (or "quit") command.
 */
public class ExitCommand extends AbstractSimpleCommand {

    public ExitCommand() {
        super("[value]", "Exit the shell.", "Causes the shell to exit with the specified integer exit value.");
    }

    @Override
    public int execute(ConsoleSession<?, ?> session, String name, List<String> args) throws InterruptedException {

        // Parse exit value, if any
        final int exitValue;
        switch (args.size()) {
        case 0:
            exitValue = 0;
            break;
        case 1:
            final String valueString = args.get(0);
            try {
                exitValue = Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                session.getErrorStream().println("Error: invalid exit value \"" + valueString + "\"");
                return 1;
            }
            break;
        default:
            this.printUsage(session, name);
            return 1;
        }

        // Tell shell sessions to exit
        if (session instanceof ShellSession) {
            if (!((ShellSession)session).setExitValue(exitValue)) {
                session.getErrorStream().println("Error: session does support exit()");
                return 1;
            }
        }

        // Done
        return exitValue;
    }
}
