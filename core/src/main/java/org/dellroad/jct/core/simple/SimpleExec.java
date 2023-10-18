
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.IOException;

import org.dellroad.jct.core.AbstractExecSession;
import org.dellroad.jct.core.Exec;
import org.dellroad.jct.core.ExecRequest;
import org.dellroad.jct.core.ExecSession;

/**
 * A simple implementation of the {@link Exec} interface using a library of {@link SimpleCommand}s.
 */
public class SimpleExec extends SimpleCommandSupport implements Exec {

// Exec

    @Override
    public ExecSession newExecSession(ExecRequest request) throws IOException {

        // Validation
        if (request == null)
            throw new IllegalArgumentException("null request");

        // Parse command line
        final FoundCommand command = this.findCommand(request.getErrorStream(), request);
        if (command == null)
            return null;

        // Build session
        return this.newExecSession(request, command);
    }

    /**
     * Alternate ssession creator for when the command line is already parsed and a command is already identified.
     *
     * @param request session request
     * @param command command to execute
     * @return new session
     * @throws IOException if an I/O error occurs
     */
    public ExecSession newExecSession(ExecRequest request, FoundCommand command) throws IOException {
        if (command == null)
            throw new IllegalArgumentException("null command");
        return new AbstractExecSession(this, request) {
            @Override
            protected int doExecute() throws InterruptedException {
                return SimpleExec.this.execute(this, command);
            }
        };
    }
}
