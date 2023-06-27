
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.List;

import org.dellroad.jct.core.JctSession;

/**
 * A {@link SimpleConsole} command.
 */
public interface SimpleCommand {

    /**
     * Get this command's usage string.
     *
     * <p>
     * The usage string should be a synopsis of the command's arguments.
     * If the command takes no arguments, this can return null or empty string.
     *
     * @param name the name under which this command is registered
     * @return command usage string
     */
    String getUsage(String name);

    /**
     * Get summarized help. This should be a single line of text, playing a role similar
     * to the first sentence (or sentence fragment) of a Javadoc comment.
     *
     * @param name the name under which this command is registered
     * @return one line command summary
     */
    String getHelpSummary(String name);

    /**
     * Get expanded help. This is typically multiple lines of text; lines should be separated by newline characters.
     *
     * @param name the name under which this command is registered
     * @return detailed command description
     */
    String getHelpDetail(String name);

    /**
     * Execute this command in the current thread and return success or failure.
     *
     * <p>
     * In the failure case, some error information should be written to standard error.
     *
     * @param session associated session
     * @param name the name under which this command was invoked
     * @param args zero or more command arguments (does not include command name)
     * @return true for success, false for failure
     * @throws InterruptedException if execution is interrupted
     */
    boolean execute(JctSession session, String name, List<String> args) throws InterruptedException;
}
