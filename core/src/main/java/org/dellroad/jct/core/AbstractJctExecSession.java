
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * Support superclass for {@link JctExecSession} implementations.
 */
public abstract class AbstractJctExecSession extends AbstractJctSession implements JctExecSession {

    protected final ExecRequest request;

    /**
     * Constructor.
     *
     * @param console associated console
     * @param request associated exec request
     * @throws IllegalArgumentException if either parameter is null
     */
    protected AbstractJctExecSession(JctConsole console, ExecRequest request) {
        super(console);
        if (request == null)
            throw new IllegalArgumentException("null request");
        this.request = request;
    }

// JctExecSession

    @Override
    public ExecRequest getExecRequest() {
        return this.request;
    }
}
