
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.IOException;

/**
 * A console component that is capable of interacting with a human via a terminal.
 * Typically this is done via some kind of read-eval-print loop.
 */
public interface Shell {

    /**
     * Create a new interactive shell session.
     *
     * <p>
     * If the request fails, this method should return null and write an error message to
     * {@code request.}{@link ShellRequest#getTerminal getTerminal()}.
     *
     * @param request interactive shell request
     * @return interactive session, or null on failure
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code request} is null
     */
    ShellSession newShellSession(ShellRequest request) throws IOException;
}
