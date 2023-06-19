
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.demo;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;

import org.dellroad.jct.core.AbstractJctSession;
import org.dellroad.jct.core.ExecRequest;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.JctExecSession;
import org.dellroad.jct.core.JctShellSession;
import org.dellroad.jct.core.ShellRequest;
//import org.jline.terminal.Terminal;

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
        final Action action;
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
            case "sleep":
                action = () -> {
                    Thread.sleep(1000 * Integer.parseInt(args.removeFirst(), 10));
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
        class Session extends AbstractJctSession implements JctExecSession {

            @Override
            protected int doExecute() throws InterruptedException {
                System.err.println(String.format("%s: demo Session performActivity() starting", Thread.currentThread().getName()));
                final int exitValue;
                try {
                    exitValue = action.execute();
                } catch (InterruptedException | RuntimeException e) {
                    System.err.println(String.format(
                      "%s: demo Session performActivity() caught %s", Thread.currentThread().getName(), e));
                    throw e;
                }
                System.err.println(String.format(
                  "%s: demo Session performActivity() done, exitValue=%d", Thread.currentThread().getName(), exitValue));
                return exitValue;
            }

            @Override
            public ExecRequest getExecRequest() {
                return request;
            }
        }
        return new Session();
    }

    @Override
    public JctShellSession newShellSession(ShellRequest request) {

        // Validate
        if (request == null)
            throw new IllegalArgumentException("null request");

        // TODO
        JctShellSession session = null;
        //session.start();

        // Done
        return session;
    }

// Action

    @FunctionalInterface
    private interface Action {
        int execute() throws InterruptedException;
    }
}
