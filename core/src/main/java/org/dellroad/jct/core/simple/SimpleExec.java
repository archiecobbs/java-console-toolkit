
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

// Public Methods

    /**
     * Alternate ssession creator for when the command line is already parsed and a command is already identified.
     *
     * @param request session request
     * @param command command to execute
     * @return new session
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if any parameter is null
     */
    public ExecSession newExecSession(ExecRequest request, FoundCommand command) throws IOException {
        return new Session(this, request, command);
    }

// Session

    /**
     * Default {@link ExecSession} implementation used by {@link SimpleExec}.
     */
    public static class Session extends AbstractExecSession {

        protected final FoundCommand command;

    // Constructor

        /**
         * Constructor.
         *
         * @param exec session owner
         * @param request command execution request
         * @param command the command to execute
         * @throws IOException if an I/O error occurs
         * @throws IllegalArgumentException if any parameter is null
         */
        public Session(SimpleExec exec, ExecRequest request, FoundCommand command) throws IOException {
            super(exec, request);
            if (command == null)
                throw new IllegalArgumentException("null command");
            this.command = command;
        }

    // AbstractConsoleSession

        @Override
        public SimpleExec getOwner() {
            return (SimpleExec)super.getOwner();
        }

        /**
         * Execute this instance's {@link #command} in the context of this session.
         *
         * <p>
         * The implementation in {@link Session} just invokes {@link FoundCommand#execute}.
         * Subclasses can override this method to intercept/wrap individual command execution.
         *
         * @return command return value
         * @throws InterruptedException if the current thread is interrupted
         */
        @Override
        protected int doExecute() throws InterruptedException {
            return this.command.execute(this);
        }
    }
}
