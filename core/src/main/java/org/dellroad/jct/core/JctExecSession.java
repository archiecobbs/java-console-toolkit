
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A {@link JctSession} associated with the direct execution of a single command.
 */
public interface JctExecSession extends JctSession {

    @Override
    default InputStream getInputStream() {
        return this.getExecRequest().getInputStream();
    }

    @Override
    default PrintStream getOutputStream() {
        return this.getExecRequest().getOutputStream();
    }

    @Override
    default PrintStream getErrorStream() {
        return this.getExecRequest().getErrorStream();
    }

    /**
     * Get the request from which this session was created.
     *
     * @return original command request
     */
    ExecRequest getExecRequest();
}
