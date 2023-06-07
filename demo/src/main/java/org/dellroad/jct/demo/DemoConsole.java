
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.demo;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.function.IntSupplier;

import org.dellroad.jct.core.AbstractActivity;
import org.dellroad.jct.core.ExecRequest;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.JctExecSession;
import org.dellroad.jct.core.JctShellSession;
import org.jline.terminal.Terminal;

/**
 * Java Console Toolkit demonstration console.
 */
public class DemoConsole implements JctConsole {

// JctConsole

    @Override
    public JctExecSession newExecSession(ExecRequest request) {

        // Validate
        if (request == null)
            throw new IllegalArgumentException("null request");

        // Parse command line into words
        ArrayDeque<String> args = new ArrayDeque<>(Arrays.asList(request.getCommand().split("\\s+", 0)));
        if (!args.isEmpty() && args.getFirst().isEmpty())       // String.trim() adds an initial empty string
            args.removeFirst();

        // Determine command to execute
        final IntSupplier action;
        if (args.isEmpty())
            action = () -> 0;
        else {
            final String command = args.removeFirst();
            switch (command) {
            case "echo":
                action = () -> {
                    boolean needSpace = false;
                    for (String arg : args) {
                        if (needSpace)
                            request.getOutputStream().print(' ');
                        else
                            needSpace = true;
                        request.getOutputStream().print(arg);
                    }
                    request.getOutputStream().println();
                    return 0;
                };
                break;
            case "date":
                action = () -> {
                    request.getOutputStream().println(new Date());
                    return 0;
                };
                break;
            default:
                action = () -> {
                    request.getOutputStream().println(String.format("%s: command not found", command));
                    return 1;
                };
                break;
            }
        }

        // Create and return new session
        class Session extends AbstractActivity implements JctExecSession {

            private volatile int exitValue;

            @Override
            protected void performActivity() throws Exception {
                System.err.println("Demo Session performActivity() starting");
                this.exitValue = action.getAsInt();
                System.err.println("Demo Session performActivity() done, exitValue=" + this.exitValue);
            }

            @Override
            public ExecRequest getExecRequest() {
                return request;
            }

            @Override
            public int getExitValue() {
                System.err.println("Demo Session returing exitValue=" + this.exitValue);
                return this.exitValue;
            }

            @Override
            protected void internalClose() {
                System.err.println("Demo Session internalClose()");
                return this.exitValue;
            }
        }
        final Session session = new Session();
        session.start();
        return session;
    }

    @Override
    public JctShellSession newShellSession(Terminal terminal) {

        // Validate
        if (terminal == null)
            throw new IllegalArgumentException("null terminal");

        // TODO
        JctShellSession session = null;
        //session.start();

        // Done
        return session;
    }
}
