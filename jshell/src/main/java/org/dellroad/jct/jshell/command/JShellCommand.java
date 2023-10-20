
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell.command;

import org.dellroad.jct.core.simple.command.SubshellCommand;
import org.dellroad.jct.jshell.JShellShell;

/**
 * A command that fires up a {@link jdk.jshell.JShell} instance.
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
          "[options]",
          "Fire up a JShell console.",
          "Creates a new JShell console and enters into it. Only works in shell mode, not execute mode."
          + "\nAccepts the same command line flags as the jshell(1) command line tool.");
    }

    /**
     * Constructor.
     *
     * @param shell creates subshell sessions
     * @param usage usage string, or null if command takes no arguments
     * @param summary help summary
     * @param detail help detail
     * @throws IllegalArgumentException if {@code shell}, {@code summary} or {@code detail} is null
     */
    public JShellCommand(JShellShell shell, String usage, String summary, String detail) {
        super(shell, usage, summary, detail);
    }
}
