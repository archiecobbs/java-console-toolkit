
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.dellroad.jct.core.AbstractConsoleRequest;
import org.dellroad.jct.core.ExecRequest;

/**
 * Straightforward implementation of the {@link ExecRequest} interface.
 */
public class SimpleExecRequest extends AbstractConsoleRequest<ExecRequest> implements ExecRequest {

    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;
    private final String commandString;
    private final List<String> commandList;

// Constructor

    /**
     * Constructor.
     *
     * @param in command input
     * @param out command output
     * @param err command error output
     * @param env environment variables, or null for an empty map
     * @param commandString the command string to execute
     * @throws IllegalArgumentException if any parameter other than {@code env} is null
     */
    public SimpleExecRequest(InputStream in, PrintStream out, PrintStream err, Map<String, String> env, String commandString) {
        this(in, out, err, env, commandString, null);
        if (commandString == null)
            throw new IllegalArgumentException("null commandString");
    }

    /**
     * Constructor.
     *
     * @param in command input
     * @param out command output
     * @param err command error output
     * @param env environment variables, or null for an empty map
     * @param commandList the command list to execute
     * @throws IllegalArgumentException if any parameter other than {@code env} is null
     */
    public SimpleExecRequest(InputStream in, PrintStream out, PrintStream err, Map<String, String> env, List<String> commandList) {
        this(in, out, err, env, null, commandList);
        if (commandList == null)
            throw new IllegalArgumentException("null commandList");
        if (commandList.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("null commandList string");
    }

    private SimpleExecRequest(InputStream in, PrintStream out, PrintStream err,
      Map<String, String> env, String commandString, List<String> commandList) {
        super(env);
        if (in == null)
            throw new IllegalArgumentException("null in");
        if (out == null)
            throw new IllegalArgumentException("null out");
        if (err == null)
            throw new IllegalArgumentException("null err");
        this.in = in;
        this.out = out;
        this.err = err;
        this.commandString = commandString;
        this.commandList = commandList;
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
    public String getCommandString() {
        return this.commandString;
    }

    @Override
    public List<String> getCommandList() {
        return this.commandList;
    }
}
