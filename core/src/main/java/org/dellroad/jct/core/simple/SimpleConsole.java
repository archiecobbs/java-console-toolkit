
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;

import org.dellroad.jct.core.AbstractJctExecSession;
import org.dellroad.jct.core.AbstractJctShellSession;
import org.dellroad.jct.core.ExecRequest;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.JctExecSession;
import org.dellroad.jct.core.JctShellSession;
import org.dellroad.jct.core.ShellRequest;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;

/**
 * A simple implementation of the {@link JctConsole} interface using a library of {@link SimpleCommand}s.
 *
 * <p>
 * Command lines are parsed by a configurable parser; see {@link #setCommandLineParser setCommandLineParser()}.
 * By default, a {@link SimpleCommandLineParser} is used.
 */
public class SimpleConsole implements JctConsole {

    private TreeMap<String, SimpleCommand> commands = new TreeMap<>();
    private Function<String, List<String>> commandLineParser = new SimpleCommandLineParser();

    public SimpleConsole() {
        this.commands.put("echo", new EchoCommand());
        this.commands.put("help", new HelpCommand());
        this.commands.put("exit", new QuitCommand());
        this.commands.put("quit", new QuitCommand());
        this.commands.put("date", new DateCommand());
        this.commands.put("sleep", new SleepCommand());
    }

    /**
     * Get the command library.
     *
     * @return mapping from command name to corresponding {@link SimpleCommand}
     */
    public NavigableMap<String, SimpleCommand> getCommands() {
        return this.commands;
    }

// Public Methods

    /**
     * Configure how command lines are parsed into separate arguments.
     *
     * @param commandLineParser breaks command lines into arguments; throws {@link IllegalArgumentException} on parse failure
     */
    public void setCommandLineParser(Function<String, List<String>> commandLineParser) {
        if (commandLineParser == null)
            throw new IllegalArgumentException("null commandLineParser");
        this.commandLineParser = commandLineParser;
    }

    /**
     * Request that the given shell session exit at the next available opportunity.
     *
     * <p>
     * This method is used by {@link QuitCommand}.
     *
     * @param session shell session to quit
     * @return true if successful, false otherwise
     */
    public boolean quit(JctShellSession session) {
        final ShellSession shellSession;
        try {
            shellSession = (ShellSession)session;
        } catch (ClassCastException e) {
            return false;
        }
        return shellSession.quit();
    }

// JctConsole

    @Override
    public JctShellSession newShellSession(ShellRequest request) throws IOException {

        // Validation
        if (request == null)
            throw new IllegalArgumentException("null request");

        // Return new shell session
        return new ShellSession(request);
    }

    @Override
    public JctExecSession newExecSession(ExecRequest request) throws IOException {

        // Validation
        if (request == null)
            throw new IllegalArgumentException("null request");

        // Parse command line
        final List<String> commandLine;
        try {
            commandLine = SimpleConsole.this.commandLineParser.apply(request.getCommand());
        } catch (IllegalArgumentException e) {
            request.getErrorStream().println(String.format("%s: %s", "Error", e.getMessage()));
            return null;
        }

        // Empty line?
        if (commandLine.isEmpty()) {
            request.getErrorStream().println(String.format("%s: %s", "Error", "empty command"));
            return null;
        }

        // Find the corresponding command
        final String commandName = commandLine.get(0);
        final SimpleCommand command = this.commands.get(commandName);
        if (command == null) {
            request.getErrorStream().println(String.format("%s: command \"%s\" not found", "Error", commandName));
            return null;
        }
        final List<String> commandArgs = commandLine.subList(1, commandLine.size());

        // Return new exec session
        return new ExecSession(request, command, commandName, commandArgs);
    }

// Internal Methods

    /**
     * Construct a {@link LineReader} for a new shell session.
     *
     * @param request associated shell request
     * @return new terminal line reader
     */
    protected LineReader buildLineReader(ShellRequest request) {
        return LineReaderBuilder.builder()
            .terminal(request.getTerminal())
            .completer(new SimpleCompleter())
          //.parser(parser)
          //.highlighter(highlighter)
            .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
            .variable(LineReader.INDENTATION, 2)
            .variable(LineReader.LIST_MAX, 100)
          //.variable(LineReader.HISTORY_FILE, Paths.get(root, "history"))
            .option(LineReader.Option.INSERT_BRACKET, true)
            .option(LineReader.Option.EMPTY_WORD_OPTIONS, false)
            .option(LineReader.Option.USE_FORWARD_SLASH, true)
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            .build();
    }

    protected String getGreeting() {
        return "Welcome to " + this.getClass().getName();
    }

    protected String getPrompt() {
        return "jct> ";
    }

// SimpleCompleter

    protected class SimpleCompleter implements Completer {

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            if (line.wordIndex() != 0)
                return;
            SimpleConsole.this.commands.forEach((name, command) -> candidates.add(this.toCandidate(name, command)));
        }

        protected Candidate toCandidate(String name, SimpleCommand command) {
            return new Candidate(name, name, null, command.getHelpSummary(name), null, UUID.randomUUID().toString(), true);
        }
    }

// ExecSession

    private class ExecSession extends AbstractJctExecSession {

        private final SimpleCommand command;
        private final String commandName;
        private final List<String> commandArgs;

        ExecSession(ExecRequest request, SimpleCommand command, String commandName, List<String> commandArgs) {
            super(SimpleConsole.this, request);
            this.command = command;
            this.commandName = commandName;
            this.commandArgs = commandArgs;
        }

        @Override
        protected boolean doExecute() throws InterruptedException {
            return this.command.execute(this, this.commandName, this.commandArgs);
        }
    }

// ShellSession

    private class ShellSession extends AbstractJctShellSession {

        private volatile boolean done;

        ShellSession(ShellRequest request) {
            super(SimpleConsole.this, request);
        }

        @Override
        protected boolean doExecute() throws InterruptedException {

            // Build line reader
            final LineReader reader = SimpleConsole.this.buildLineReader(this.request);

            // Send greeting
            this.out.println(SimpleConsole.this.getGreeting());

            // Run command loop
            try {
                return this.commandLoop(reader);
            } catch (Throwable e) {
                e.printStackTrace(out);
                return false;
            }
        }

        protected boolean commandLoop(LineReader reader) {
            while (!this.done) {

                // Read command line
                String line;
                try {
                    line = reader.readLine(SimpleConsole.this.getPrompt());
                } catch (EndOfFileException e) {
                    break;
                } catch (UserInterruptException e) {
                    continue;
                }

                // Parse command line
                final List<String> commandLine;
                try {
                    commandLine = SimpleConsole.this.commandLineParser.apply(line);
                } catch (IllegalArgumentException e) {
                    this.out.println(String.format("%s: %s", "Error", e.getMessage()));
                    continue;
                }
                if (commandLine.isEmpty())
                    continue;

                // Find the corresponding command
                final String commandName = commandLine.get(0);
                final SimpleCommand command = SimpleConsole.this.commands.get(commandName);
                if (command == null) {
                    this.out.println(String.format("%s: command not found", commandName));
                    continue;
                }

                // Execute command
                final List<String> commandArgs = commandLine.subList(1, commandLine.size());
                try {
                    command.execute(this, commandName, commandArgs);
                } catch (InterruptedException e) {
                    reader.getTerminal().flush();      // push out the "^C" that the terminal inserts
                    this.out.println();
                } catch (Exception e) {
                    e.printStackTrace(out);
                }
            }
            return true;
        }

        public boolean quit() {
            this.done = true;
            return true;
        }
    }
}
