
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * A {@link JctSession} associated with the direct execution of a single command.
 */
public interface JctExecSession extends JctSession {

    /**
     * Get the request from which this session was created.
     *
     * @return original command request
     */
    ExecRequest getExecRequest();
}
