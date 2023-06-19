
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

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
