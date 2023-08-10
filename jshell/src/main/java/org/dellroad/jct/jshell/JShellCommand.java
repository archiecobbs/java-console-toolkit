
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import org.dellroad.jct.core.simple.command.SubshellCommand;

/**
 * A command that fires up a {@link jdk.jshell.JShell} instance.
 *
 * <p>
 * See {@link JShellShellSession} for details.
 */
public class JShellCommand extends SubshellCommand {

    /**
     * Default constructor.
     *
     * <p>
     * Creates an instance using a new anonymous {@link JShellShell} instance.
     */
    public JShellCommand() {
        this(new JShellShell());
    }

    /**
     * Constructor.
     *
     * <p>
     * This constructor provides default usage, summary, and description strings.
     *
     * @param shell creates subshell sessions
     * @throws IllegalArgumentException if {@code shell} is null
     */
    public JShellCommand(JShellShell shell) {
        this(shell,
          "Fire up a JShell console.",
          "Creates a new JShell console and enters into it. Only works in shell mode, not execute mode."
          + "\nAccepts the same command line flags as the jshell(1) command line tool. For example, add"
          + "\n\"--execution=local\" to configure local execution, which makes it possible to access"
          + "\nobjects in the current JVM.");
    }

    /**
     * Primary constructor.
     *
     * @param shell creates subshell sessions
     * @param summary help summary
     * @param detail help detail
     * @throws IllegalArgumentException if any parameter is null
     */
    public JShellCommand(JShellShell shell, String summary, String detail) {
        super(shell, "[options]", summary, detail);
    }
}
