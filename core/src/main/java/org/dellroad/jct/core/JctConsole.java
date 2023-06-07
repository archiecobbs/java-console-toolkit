
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.IOException;

import org.jline.terminal.Terminal;

/**
 * A command line interface (CLI) console.
 *
 * <p>
 * A console is essentially a factory for console sessions, of which there are two types:
 * <ul>
 *  <li>{@link JctShellSession} - an interactive session typically supporting keyboard editing, history, etc.
 *  <li>{@link JctExecSession} - a non-interactive session in which a single command is executed.
 * </ul>
 */
public interface JctConsole {

    /**
     * Create a new interactive console session.
     *
     * @param terminal JLine terminal already configured approrpriately
     * @return interactive session
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code terminal} is null
     */
    JctShellSession newShellSession(Terminal terminal) throws IOException;

    /**
     * Create a new non-interactive console session in which to execute a single command.
     *
     * <p>
     * The command to execute is returned by {@code request.}{@link ExecRequest#getCommand getCommand()};
     * typically this string contains a command name and zero or more arguments separated by whitespace.
     * However, the implementation is free to determine if/how this string should be parsed (e.g., whether to
     * support quoting, backslash escapes, etc.). If a parse fails, typically an error is written to
     * {@code request.}{@link ExecRequest#getErrorStream getErrorStream()}.
     *
     * @param request command request
     * @return non-interactive session
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code request} null
     */
    JctExecSession newExecSession(ExecRequest request) throws IOException;
}
