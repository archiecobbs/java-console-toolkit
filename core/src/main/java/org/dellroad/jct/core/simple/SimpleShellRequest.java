
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.List;
import java.util.Map;

import org.dellroad.jct.core.AbstractConsoleRequest;
import org.dellroad.jct.core.ShellRequest;
import org.jline.terminal.Terminal;

/**
 * Straightforward implementation of the {@link ShellRequest} interface.
 */
public class SimpleShellRequest extends AbstractConsoleRequest<ShellRequest> implements ShellRequest {

    private final Terminal terminal;
    private final List<String> args;

// Constructor

    /**
     * Constructor.
     *
     * @param terminal associated interactive terminal
     * @param args shell arguments
     * @param env environment variables, or null for an empty map
     * @throws IllegalArgumentException if {@code terminal} or {@code args} is null
     */
    public SimpleShellRequest(Terminal terminal, List<String> args, Map<String, String> env) {
        super(env);
        if (terminal == null)
            throw new IllegalArgumentException("null terminal");
        if (args == null)
            throw new IllegalArgumentException("null args");
        this.terminal = terminal;
        this.args = args;
    }

// ShellRequest

    @Override
    public Terminal getTerminal() {
        return this.terminal;
    }

    @Override
    public List<String> getShellArguments() {
        return this.args;
    }
}
