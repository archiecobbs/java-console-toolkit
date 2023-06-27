
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.Date;
import java.util.List;

import org.dellroad.jct.core.JctSession;

/**
 * A simple "date" command that can be used with a {@link SimpleConsole}.
 */
public class DateCommand extends AbstractSimpleCommand {

    public DateCommand() {
        super(null, "Display the current time and date.", "Displays the current time and date using java.util.Date.toString().");
    }

    @Override
    public boolean execute(JctSession session, String name, List<String> args) throws InterruptedException {
        switch (args.size()) {
        case 0:
            session.getOutputStream().println(new Date());
            return true;
        default:
            this.printUsage(session, name);
            return false;
        }
    }
}
