
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import org.jline.terminal.Terminal;

/**
 * A {@link JctSession} associated with an interactive shell.
 */
public interface JctShellSession extends JctSession {

    /**
     * Get the associated {@link Terminal}.
     *
     * @return the interactive shell's associated {@link Terminal}
     */
    Terminal getTerminal();
}
