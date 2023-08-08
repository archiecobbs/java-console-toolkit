
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Represents a request to create a new {@link ExecSession} to execute a command.
 *
 * <p>
 * The command is specified in one of two ways: as a single string (via {@link #getCommandString}) or as an array of strings
 * (via {@link #getCommandList}). In the former case, this instance is responsible for whatever parsing is needed; the
 * "command" could actually be more like a script, e.g., {@code "echo foo; echo bar;"}.
 */
public interface ExecRequest extends ConsoleRequest<ExecRequest> {

    /**
     * Get this request's input stream.
     *
     * @return command input
     */
    InputStream getInputStream();

    /**
     * Get this request's normal output stream.
     *
     * @return command output
     */
    PrintStream getOutputStream();

    /**
     * Get this request's error output stream.
     *
     * @return command error output
     */
    PrintStream getErrorStream();

    /**
     * Get this request's command to execute as a single string.
     *
     * <p>
     * The command is given as a single string. Typically the string is parsed somehow into a distinguisble command.
     *
     * @return the command to execute, or null if {@link #getCommandList} returns a non-null array
     */
    String getCommandString();

    /**
     * Get this request's command to execute as a string array.
     *
     * @return the command to execute, or null if {@link #getCommandString} returns a non-null command string
     */
    List<String> getCommandList();
}
