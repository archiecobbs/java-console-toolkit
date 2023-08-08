
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * Support superclass for {@link ExecSession} implementations.
 */
public abstract class AbstractExecSession extends AbstractConsoleSession<Exec, ExecRequest> implements ExecSession {

    /**
     * Constructor.
     *
     * @param owner session owner
     * @param request associated request
     * @throws IllegalArgumentException if either parameter is null
     */
    protected AbstractExecSession(Exec owner, ExecRequest request) {
        super(owner, request);
    }
}
