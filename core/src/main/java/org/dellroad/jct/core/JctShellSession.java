
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
     * Get the request from which this session was created.
     *
     * @return original command request
     */
    ShellRequest getShellRequest();
}
