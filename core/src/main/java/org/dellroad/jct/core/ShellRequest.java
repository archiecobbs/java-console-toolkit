
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.List;

import org.jline.terminal.Terminal;

/**
 * Represents a request to create a new {@link ShellSession} for executing commands interactively.
 */
public interface ShellRequest extends ConsoleRequest<ShellRequest> {

    /**
     * Get the associated {@link Terminal}.
     *
     * @return the shell's associated {@link Terminal}
     */
    Terminal getTerminal();

    /**
     * Get the command line arguments for the shell.
     *
     * <p>
     * How these are interpreted is up to the specific shell being invoked.
     * Some may simply ignore them.
     *
     * @return zero or more shell command line arguments, never null
     */
    List<String> getShellArguments();
}
