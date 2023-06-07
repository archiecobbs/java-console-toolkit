
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.Map;

import org.jline.terminal.Terminal;

/**
 * Represents a request to execute commands interactively.
 */
public interface ShellRequest {

    /**
     * Get the associated {@link Terminal}.
     *
     * @return the interactive shell's associated {@link Terminal}
     */
    Terminal getTerminal();

    /**
     * Get the shell's environment variables.
     *
     * @return environment variables
     */
    Map<String, String> getEnvironment();
}
