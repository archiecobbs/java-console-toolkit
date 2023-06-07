
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

/**
 * Straightforward implementation of the {@link ExecRequest} interface.
 */
public class SimpleExecRequest implements ExecRequest {

    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;
    private final Map<String, String> env;
    private final String command;

    /**
     * Constructor.
     *
     * @param in command input
     * @param out command output
     * @param err command error output
     * @param env environment variables, or null for an empty map
     * @param command the command to execute
     * @throws IllegalArgumentException if any parameter other than {@code env} is null
     */
    public SimpleExecRequest(InputStream in, PrintStream out, PrintStream err, Map<String, String> env, String command) {

        // Validate
        if (in == null)
            throw new IllegalArgumentException("null in");
        if (out == null)
            throw new IllegalArgumentException("null out");
        if (err == null)
            throw new IllegalArgumentException("null err");
        if (command == null)
            throw new IllegalArgumentException("null command");

        // Initialize
        this.in = in;
        this.out = out;
        this.err = err;
        this.env = env;
        this.command = command;
    }

// ExecRequest

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public PrintStream getOutputStream() {
        return this.out;
    }

    @Override
    public PrintStream getErrorStream() {
        return this.err;
    }

    @Override
    public Map<String, String> getEnvironment() {
        return this.env != null ? this.env : Collections.emptyMap();
    }

    @Override
    public String getCommand() {
        return this.command;
    }
}
