
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.IOException;

/**
 * A console component that is capable of executing individual commands.
 */
public interface Exec {

    /**
     * Create a new non-interactive console session in which to execute a single command.
     *
     * <p>
     * If the request fails, this method should return null and write an error message to
     * {@code request.}{@link ExecRequest#getErrorStream getErrorStream()}.
     *
     * @param request command request
     * @return non-interactive session, or null on failure
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code request} null
     */
    ExecSession newExecSession(ExecRequest request) throws IOException;
}
