
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.util.ArrayList;
import java.util.List;

import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.simple.command.SubshellCommand;

/**
 * A command that fires up a {@link jdk.jshell.JShell} instance.
 *
 * <p>
 * The {@link jdk.jshell.JShell} instance can be customized in two ways:
 * <ul>
 *  <li>Overriding {@link JShellShell#createBuilder JShellShell.createBuilder()} allows customization of the
 *      {@link JavaShellToolBuilder} that is used to build new JShell instances. Supply the customized
 *      {@link JShellShell} instances when constructing instances of this class.
 *  <li>Overriding {@link #buildJShellParams buildJShellParams()} allows customization of the flags and parameters
 *      passed to JShell itself (these are the same as accepted by the {@code jshell(1)} command line tool).
 *      By default, the parameters passed are the just parameters given to this command on the command line.
 * </ul>
 *
 * <p>
 * This implementation also registers an {@link ExecutionControl} implementation that is similar to {@link LocalExecutionControl},
 * but delegates to the current thread's context loader as its parent class loader. This fixes issues with finding local classes
 * in certain more complex class loading situations such as running in a web container. Use {@link #enableLocalContextExecution}
 * automatically to enable this behavior.
 */
public class JShellCommand extends SubshellCommand {

    private boolean localContextExecution;

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

// Public Methods

    /**
     * Enable local execution along with workarounds for class discovery at both the source and execution
     * layers through the context class loader.
     *
     * @return this instance
     */
    public JShellCommand enableLocalContextExecution() {
        this.localContextExecution = true;
        return this;
    }

// SubshellCommand

    @Override
    protected ShellRequest buildShellRequest(ShellSession session, String name, List<String> params) {
        return super.buildShellRequest(session, name, this.buildJShellParams(params));
    }

// Subclass Methods

    /**
     * Generate a list of command line flags and parameters to be passed to the JShell tool,
     * given the arguments given on the shell command line for this command.
     *
     * <p>
     * If this method is overridden such that this command's parameters are interpreted differently,
     * then the full constructor taking customized help detail should be used.
     *
     * <p>
     * The implementation in {@link JShellCommand} just returns the list unmodified unless
     * {@link #enableLocalContextExecution} has been invoked, in which case the parameters
     * returned by {@link LocalContextExecutionControlProvider#generateJShellFlags} are
     * prepended.
     *
     * @param commandLineParams parameters given to the shell command line
     * @return flags and parameters for JShell
     * @throws IllegalArgumentException if {@code commandLineParams} is null
     */
    protected List<String> buildJShellParams(List<String> commandLineParams) {
        if (commandLineParams == null)
            throw new IllegalArgumentException("null commandLineParams");
        if (!this.localContextExecution)
            return commandLineParams;
        final ArrayList<String> newParams = new ArrayList<>();
        newParams.addAll(LocalContextExecutionControlProvider.generateJShellFlags(this.getSourceClassLoader()));
        newParams.addAll(commandLineParams);
        return newParams;
    }

    /**
     * Choose the {@link ClassLoader} that we examine to build the source {@code --class-path}
     * when the local class loading workaround is enabled.
     *
     * <p>
     * The implementation in {@link JShellCommand} always returns null.
     *
     * @return class loader for source compilation, or null to use the current thread's context class loader
     * @see LocalContextExecutionControlProvider#generateJShellFlags
     */
    protected ClassLoader getSourceClassLoader() {
        return null;
    }
}
