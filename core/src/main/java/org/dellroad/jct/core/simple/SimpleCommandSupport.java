
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.ExecRequest;

/**
 * Support superclass for console components that utilize {@link SimpleCommand}s.
 */
public class SimpleCommandSupport {

    protected CommandLineParser commandLineParser = new SimpleCommandLineParser();
    protected CommandRegistry commandRegistry = Collections::emptySortedMap;

    /**
     * Get the configured command line parser.
     *
     * @return command line parser, never null
     */
    public CommandLineParser getCommandLineParser() {
        return this.commandLineParser;
    }

    /**
     * Configure how command lines are parsed into separate arguments.
     *
     * <p>
     * By default, a {@link SimpleCommandLineParser} is used.
     *
     * @param commandLineParser command line parser
     * @throws IllegalArgumentException if {@code commandLineParser} is null
     */
    public void setCommandLineParser(CommandLineParser commandLineParser) {
        if (commandLineParser == null)
            throw new IllegalArgumentException("null commandLineParser");
        this.commandLineParser = commandLineParser;
    }

    /**
     * Get the configured command registry.
     *
     * @return command registry, never null
     */
    public CommandRegistry getCommandRegistry() {
        return this.commandRegistry;
    }

    /**
     * Configure the registry of available commands.
     *
     * @param commandRegistry registry of commands
     * @throws IllegalArgumentException if {@code commandRegistry} is null
     */
    public void setCommandRegistry(CommandRegistry commandRegistry) {
        if (commandRegistry == null)
            throw new IllegalArgumentException("null commandRegistry");
        this.commandRegistry = commandRegistry;
    }

// Subclass Methods

    /**
     * Find the command in the registry corresponding to the given request.
     *
     * <p>
     * If an error occurs, an error message is printed to {@code errout} and null is returned.
     *
     * @param errout error output
     * @param request exec request
     * @return successful parse and lookup, otherwise null
     * @throws IllegalArgumentException if either parameter is null
     */
    public FoundCommand findCommand(PrintStream errout, ExecRequest request) {
        if (request == null)
            throw new IllegalArgumentException("null request");
        return this.findCommand(errout, request.getCommandString(), request.getCommandList());
    }

    /**
     * Find the command in the registry corresponding to the given command string, which will be parsed.
     *
     * <p>
     * If an error occurs, an error message is printed to {@code errout} and null is returned.
     *
     * @param errout error output
     * @param commandString command string
     * @return successful parse and lookup, otherwise null
     * @throws IllegalArgumentException if either parameter is null
     */
    public FoundCommand findCommand(PrintStream errout, String commandString) {
        if (commandString == null)
            throw new IllegalArgumentException("null commandString");
        return this.findCommand(errout, commandString, null);
    }

    /**
     * Find the command in the registry corresponding to the given command list.
     *
     * <p>
     * If an error occurs, an error message is printed to {@code errout} and null is returned.
     *
     * @param errout error output
     * @param commandList command list
     * @return successful parse and lookup, otherwise null
     * @throws IllegalArgumentException if either parameter is null
     */
    public FoundCommand findCommand(PrintStream errout, List<String> commandList) {
        if (commandList == null)
            throw new IllegalArgumentException("null commandList");
        return this.findCommand(errout, null, commandList);
    }

    private FoundCommand findCommand(PrintStream errout, String commandString, List<String> commandList) {

        // Validation
        if (errout == null)
            throw new IllegalArgumentException("null errout");
        if (commandString == null && commandList == null)
            throw new IllegalArgumentException("null command");

        // Parse command string if needed
        if (commandList == null) {
            try {
                commandList = this.commandLineParser.parseCommandLine(commandString);
            } catch (CommandLineParser.SyntaxException e) {
                errout.println(String.format("%s@%d: %s", "Error", e.getOffset(), e.getMessage()));
                return null;
            }
        }

        // Empty line?
        if (commandList.isEmpty()) {
            errout.println(String.format("%s: %s", "Error", "empty command"));
            return null;
        }

        // Find the corresponding command
        final String name = commandList.get(0);
        if (this.commandRegistry == null) {
            errout.println(String.format("%s: no commands are configured", "Error"));
            return null;
        }
        final SimpleCommand command = this.commandRegistry.getCommands().get(name);
        if (command == null) {
            errout.println(String.format("%s: command \"%s\" not found", "Error", name));
            return null;
        }
        final List<String> params = commandList.subList(1, commandList.size());

        // Done
        return new FoundCommand(command, name, params);
    }

    /**
     * Execute the given command in the context of the given session.
     *
     * <p>
     * The implementation in {@link SimpleCommandSupport} just invokes {@link FoundCommand#execute}.
     * Subclasses can override this method to intercept/wrap individual command execution.
     *
     * @param session execution context
     * @param command command to execute
     * @return command return value
     * @throws IllegalArgumentException if either parameter is null
     * @throws InterruptedException if the current thread is interrupted
     */
    protected int execute(ConsoleSession<?, ?> session, FoundCommand command) throws InterruptedException {

        // Validation
        if (session == null)
            throw new IllegalArgumentException("null session");
        if (command == null)
            throw new IllegalArgumentException("null command");

        // Execute
        return command.execute(session);
    }

// FoundCommand

    /**
     * Represents a command line that has been parsed and the corresponding {@link SimpleCommand} found.
     */
    public static class FoundCommand {

        private final SimpleCommand command;
        private final String name;
        private final List<String> parameters;

        /**
         * Constructor.
         *
         * @param command the command to execute
         * @param name command name
         * @param parameters command parameters
         * @throws IllegalArgumentException if any parameter is null
         */
        public FoundCommand(SimpleCommand command, String name, List<String> parameters) {
            if (command == null)
                throw new IllegalArgumentException("null command");
            if (name == null)
                throw new IllegalArgumentException("null name");
            if (parameters == null)
                throw new IllegalArgumentException("null parameters");
            if (parameters.stream().anyMatch(Objects::isNull))
                throw new IllegalArgumentException("null parameter");
            this.command = command;
            this.name = name;
            this.parameters = parameters;
        }

        public SimpleCommand getCommand() {
            return this.command;
        }

        public String getName() {
            return this.name;
        }

        public List<String> getParameters() {
            return this.parameters;
        }

        /**
         * Execute this command.
         *
         * @param session current session
         * @return zero if successful, non-zero error code if an error occurred
         * @throws InterruptedException if the current thread is interrupted
         */
        public int execute(ConsoleSession<?, ?> session) throws InterruptedException {
            return this.command.execute(session, this.name, this.parameters);
        }
    }
}
