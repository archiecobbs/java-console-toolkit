
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.dellroad.jct.core.AbstractShellSession;
import org.dellroad.jct.core.Shell;
import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.ShellSession;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;

/**
 * A simple implementation of the {@link Shell} interface based on a library of {@link SimpleCommand}s.
 */
public class SimpleShell extends SimpleCommandSupport implements Shell {

// Shell

    @Override
    public ShellSession newShellSession(ShellRequest request) throws IOException {

        // Validation
        if (request == null)
            throw new IllegalArgumentException("null request");

        // Return new shell session
        return this.newShellSession(request, this.buildLineReader(request));
    }

// Public Methods

    /**
     * Alternate ssession creator for when a {@link LineReader} is already constructed.
     *
     * @param request session request
     * @param reader console line reader
     * @return new session
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if any parameter is null
     */
    public ShellSession newShellSession(ShellRequest request, LineReader reader) throws IOException {
        return new Session(this, request, reader);
    }

    /**
     * Get the welcome greeting.
     *
     * @return welcome greeting, or null for none
     */
    public String getGreeting() {
        return "Welcome to " + this.getClass().getName();
    }

    /**
     * Get the normal prompt.
     *
     * @return the normal prompt
     */
    public String getNormalPrompt() {
        return "jct> ";
    }

    /**
     * Get the continuation line prompt.
     *
     * @return the continuation line prompt
     */
    public String getContinuationPrompt() {
        return "...> ";
    }

// Internal Methods

    /**
     * Construct a {@link LineReader} for a new shell session.
     *
     * <p>
     * The implementation in {@link SimpleShell} invokes {@link #createLineReaderBuilder}
     * to create and configure the builder, then just returns {@link LineReaderBuilder#build}.
     *
     * @param request associated shell request
     * @return new terminal line reader
     */
    protected LineReader buildLineReader(ShellRequest request) {
        return this.createLineReaderBuilder(request).build();
    }

    /**
     * Create and configure a {@link LineReaderBuilder} for a new shell session.
     *
     * @param request associated shell request
     * @return builder for terminal line reader
     */
    protected LineReaderBuilder createLineReaderBuilder(ShellRequest request) {
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
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true);
    }

// SimpleCompleter

    /**
     * A simple {@link Completer} for command names.
     */
    protected class SimpleCompleter implements Completer {

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            if (line.wordIndex() != 0)
                return;
            SimpleShell.this.buildCommandMap().forEach(
              (name, command) -> candidates.add(this.toCandidate(name, command)));
        }

        protected Candidate toCandidate(String name, SimpleCommand command) {
            return new Candidate(name, name, null, command.getHelpSummary(name), null, UUID.randomUUID().toString(), true);
        }
    }

// Session

    /**
     * Default {@link ShellSession} implementation used by {@link SimpleShell}.
     */
    public static class Session extends AbstractShellSession {

        protected final LineReader reader;

    // Constructor

        /**
         * Constructor.
         *
         * @param shell session owner
         * @param request shell execution request
         * @param reader console line reader
         * @throws IOException if an I/O error occurs
         * @throws IllegalArgumentException if any parameter is null
         */
        public Session(SimpleShell shell, ShellRequest request, LineReader reader) throws IOException {
            super(shell, request);
            if (reader == null)
                throw new IllegalArgumentException("null reader");
            this.reader = reader;
        }

    // AbstractConsoleSession

        @Override
        public SimpleShell getOwner() {
            return (SimpleShell)super.getOwner();
        }

        @Override
        protected int doExecute() throws InterruptedException {

            // Send greeting, if any
            Optional.ofNullable(this.getGreeting())
              .ifPresent(this.getOutputStream()::println);

            // Run command loop
            try {
                this.commandLoop();
            } catch (Throwable e) {
                e.printStackTrace(out);
                return 1;
            }

            // Done
            return this.getExitValue();
        }

        /**
         * Get the welcome greeting.
         *
         * <p>
         * The implementation in {@link Session} delegates to {@link SimpleShell#getGreeting}.
         *
         * @return welcome greeting, or null for none
         */
        public String getGreeting() {
            return this.getOwner().getGreeting();
        }

        /**
         * Get the normal prompt.
         *
         * <p>
         * The implementation in {@link Session} delegates to {@link SimpleShell#getNormalPrompt}.
         *
         * @return the normal prompt
         */
        public String getNormalPrompt() {
            return this.getOwner().getNormalPrompt();
        }

        /**
         * Get the continuation line prompt.
         *
         * <p>
         * The implementation in {@link Session} delegates to {@link SimpleShell#getContinuationPrompt}.
         *
         * @return the continuation line prompt
         */
        public String getContinuationPrompt() {
            return this.getOwner().getContinuationPrompt();
        }

    // Internal Methods

        protected void commandLoop() {
        mainLoop:
            while (this.exitValue == null) {

                // Initialize new multi-line command line
                StringBuilder buf = new StringBuilder();
                List<String> commandLine;
                while (true) {

                    // First line or continuation line?
                    final boolean firstLine = buf.length() == 0;
                    final String prompt = firstLine ? this.getNormalPrompt() : this.getContinuationPrompt();

                    // Read the next single line of input
                    String line;
                    try {
                        line = this.reader.readLine(prompt);
                    } catch (EndOfFileException e) {
                        break mainLoop;
                    } catch (UserInterruptException e) {
                        continue mainLoop;
                    }

                    // Append to current (multi-line) command
                    if (!firstLine)
                        buf.append('\n');
                    buf.append(line);

                    // Parse entire (multi-line) command
                    try {
                        commandLine = this.getOwner().commandLineParser.parseCommandLine(buf.toString());
                    } catch (CommandLineParser.SyntaxException e) {
                        this.getErrorStream().println(String.format("%s: %s", "Error", e.getMessage()));
                        continue;
                    }

                    // Was that the last line?
                    if (commandLine != null)
                        break;
                }

                // Empty command?
                if (commandLine.isEmpty())
                    continue;

                // Find command
                final FoundCommand command = this.getOwner().findCommand(this.getErrorStream(), commandLine);
                if (command == null)
                    continue;

                // Execute command
                try {
                    this.execute(command);
                } catch (InterruptedException e) {
                    this.reader.getTerminal().flush();          // push out the "^C" that the terminal inserts
                    this.getOutputStream().println();
                } catch (Exception e) {
                    e.printStackTrace(out);
                }
            }
        }

        /**
         * Execute the given command in the context of this session.
         *
         * <p>
         * The implementation in {@link Session} just invokes {@link FoundCommand#execute}.
         * Subclasses can override this method to intercept/wrap individual command execution.
         *
         * @param command command to execute
         * @return command return value
         * @throws InterruptedException if the current thread is interrupted
         * @throws IllegalArgumentException if {@code command} is null
         */
        protected int execute(FoundCommand command) throws InterruptedException {
            if (command == null)
                throw new IllegalArgumentException("null command");
            return command.execute(this);
        }
    }
}
