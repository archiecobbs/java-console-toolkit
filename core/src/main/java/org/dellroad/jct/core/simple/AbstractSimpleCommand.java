
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import org.dellroad.jct.core.JctSession;

/**
 * Support superclass for {@link SimpleCommand} implementations.
 */
public abstract class AbstractSimpleCommand implements SimpleCommand {

    private final String usage;
    private final String summary;
    private final String detail;

    /**
     * Constructor.
     *
     * @param usage usage string, or null if command takes no arguments
     * @param summary help summary
     * @param detail help detail
     * @throws IllegalArgumentException if {@code summary} or {@code detail} is null
     */
    protected AbstractSimpleCommand(String usage, String summary, String detail) {
        if (summary == null)
            throw new IllegalArgumentException("null summary");
        if (detail == null)
            throw new IllegalArgumentException("null detail");
        this.usage = usage;
        this.summary = summary;
        this.detail = detail;
    }

// SimpleCommand

    @Override
    public String getUsage(String name) {
        return this.usage;
    }

    @Override
    public String getHelpSummary(String name) {
        return this.summary;
    }

    @Override
    public String getHelpDetail(String name) {
        return this.detail;
    }

// Internal Methods

    protected SimpleConsole getSimpleConsole(JctSession session, String name) {
        try {
            return (SimpleConsole)session.getConsole();
        } catch (ClassCastException e) {
            session.getErrorStream().println(String.format(
              "Error: this \"%s\" command requires a %s", name, SimpleConsole.class.getName()));
            return null;
        }
    }

    protected void printUsage(JctSession session, String name) {
        session.getErrorStream().print(String.format("Usage: %s", name));
        final String args = this.getUsage(name);
        if (args != null && !args.isEmpty())
            session.getErrorStream().print(String.format(" %s", args));
        session.getErrorStream().println();
    }
}
