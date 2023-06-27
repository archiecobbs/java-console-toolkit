
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.List;

import org.dellroad.jct.core.JctSession;

/**
 * A simple "sleep" command that can be used with a {@link SimpleConsole}.
 */
public class SleepCommand extends AbstractSimpleCommand {

    public SleepCommand() {
        super("seconds", "Sleep for a while.", "Sleeps for the specified number of seconds. Fractional seconds are supported.");
    }

    @Override
    public boolean execute(JctSession session, String name, List<String> args) throws InterruptedException {

        // Get seconds
        final long millis;
        switch (args.size()) {
        case 1:
            final String secs = args.get(0);
            try {
                millis = (long)(Double.parseDouble(secs) * 1000.0);
            } catch (NumberFormatException e) {
                session.getErrorStream().println(String.format("Error: invalid seconds \"%s\"", secs));
                return false;
            }
            break;
        default:
            this.printUsage(session, name);
            return false;
        }

        // Sleep
        Thread.sleep(millis);
        return true;
    }
}
