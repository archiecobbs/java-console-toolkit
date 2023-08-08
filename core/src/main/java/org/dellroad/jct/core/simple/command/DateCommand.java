
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import java.util.Date;
import java.util.List;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.simple.AbstractSimpleCommand;

/**
 * A simple "date" command.
 */
public class DateCommand extends AbstractSimpleCommand {

    public DateCommand() {
        super(null, "Display the current time and date.", "Displays the current time and date using java.util.Date.toString().");
    }

    @Override
    public int execute(ConsoleSession<?, ?> session, String name, List<String> args) throws InterruptedException {
        switch (args.size()) {
        case 0:
            session.getOutputStream().println(new Date());
            return 0;
        default:
            this.printUsage(session, name);
            return 1;
        }
    }
}
