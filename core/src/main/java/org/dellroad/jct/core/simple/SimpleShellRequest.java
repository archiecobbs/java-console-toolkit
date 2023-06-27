
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.Map;

import org.dellroad.jct.core.ShellRequest;
import org.jline.terminal.Terminal;

/**
 * Straightforward implementation of the {@link ShellRequest} interface.
 */
public class SimpleShellRequest extends AbstractRequest implements ShellRequest {

    private final Terminal terminal;

    /**
     * Constructor.
     *
     * @param terminal associated interactive terminal
     * @param env environment variables, or null for an empty map
     * @throws IllegalArgumentException if {@code terminal} is null
     */
    public SimpleShellRequest(Terminal terminal, Map<String, String> env) {
        super(env);

        // Validate
        if (terminal == null)
            throw new IllegalArgumentException("null terminal");

        // Initialize
        this.terminal = terminal;
    }

// ShellRequest

    @Override
    public Terminal getTerminal() {
        return this.terminal;
    }
}
