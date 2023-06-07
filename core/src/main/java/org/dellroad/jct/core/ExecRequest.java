
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * Represents a request to directly (i.e., non-interactively) execute a command.
 */
public interface ExecRequest {

    /**
     * Get the command's input stream.
     *
     * @return command input
     */
    InputStream getInputStream();

    /**
     * Get the command's output stream.
     *
     * @return command output
     */
    PrintStream getOutputStream();

    /**
     * Get the command's error output stream.
     *
     * @return command error output
     */
    PrintStream getErrorStream();

    /**
     * Get the command's environment variables.
     *
     * @return environment variables
     */
    Map<String, String> getEnvironment();

    /**
     * Get the requested command.
     *
     * <p>
     * Typically the returned string must be parsed somehow; see {@link JctConsole#newExecSession}.
     *
     * @return the command to execute
     */
    String getCommand();
}
