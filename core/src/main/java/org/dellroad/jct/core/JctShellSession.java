
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.PrintStream;

/**
 * A {@link JctSession} associated with an interactive shell.
 *
 * <p>
 * Note that interactive shell sessions do not have distinct output and error streams. Instead, there is
 * only one output stream, namely, the stream that ultimately gets displayed on the user's terminal.
 */
public interface JctShellSession extends JctSession {

    @Override
    default PrintStream getErrorStream() {
        return this.getOutputStream();
    }

    /**
     * Get the request from which this session was created.
     *
     * @return original command request
     */
    ShellRequest getShellRequest();
}
