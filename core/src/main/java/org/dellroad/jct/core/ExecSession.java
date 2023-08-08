
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A {@link ConsoleSession} associated with the direct execution of a single command.
 */
public interface ExecSession extends ConsoleSession<Exec, ExecRequest> {

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation in {@link ExecSession} just returns
     * {@link #getRequest this.getRequest()}{@code .}{@link ExecRequest#getInputStream getInputStream()}.
     */
    @Override
    default InputStream getInputStream() {
        return this.getRequest().getInputStream();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation in {@link ExecSession} just returns
     * {@link #getRequest this.getRequest()}{@code .}{@link ExecRequest#getOutputStream getOutputStream()}.
     */
    @Override
    default PrintStream getOutputStream() {
        return this.getRequest().getOutputStream();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation in {@link ExecSession} just returns
     * {@link #getRequest this.getRequest()}{@code .}{@link ExecRequest#getErrorStream getErrorStream()}.
     */
    @Override
    default PrintStream getErrorStream() {
        return this.getRequest().getErrorStream();
    }
}
