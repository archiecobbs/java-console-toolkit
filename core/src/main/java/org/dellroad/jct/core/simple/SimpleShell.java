
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.IOException;
import java.util.List;
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
        return new Session(request, this.buildLineReader(request));
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

    protected String getGreeting() {
        return "Welcome to " + this.getClass().getName();
    }

    protected String getNormalPrompt() {
        return "jct> ";
    }

    protected String getContinuationPrompt() {
        return "...> ";
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
            SimpleShell.this.commandRegistry.getCommands().forEach(
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
    protected class Session extends AbstractShellSession {

        protected final LineReader reader;

        protected Session(ShellRequest request, LineReader reader) throws IOException {
            super(SimpleShell.this, request);
            if (reader == null)
                throw new IllegalArgumentException("null reader");
            this.reader = reader;
        }

        @Override
        protected int doExecute() throws InterruptedException {

            // Send greeting
            this.getOutputStream().println(SimpleShell.this.getGreeting());

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

        protected void commandLoop() {
        mainLoop:
            while (this.exitValue == null) {

                // Initialize new multi-line command line
                StringBuilder buf = new StringBuilder();
                List<String> commandLine;
                while (true) {

                    // First line or continuation line?
                    final boolean firstLine = buf.length() == 0;
                    final String prompt = firstLine ? SimpleShell.this.getNormalPrompt() : SimpleShell.this.getContinuationPrompt();

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
                        commandLine = SimpleShell.this.commandLineParser.parseCommandLine(buf.toString());
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
                final FoundCommand command = SimpleShell.this.findCommand(this.getErrorStream(), commandLine);
                if (command == null)
                    continue;

                // Execute command
                try {
                    SimpleShell.this.execute(this, command);
                } catch (InterruptedException e) {
                    this.reader.getTerminal().flush();          // push out the "^C" that the terminal inserts
                    this.getOutputStream().println();
                } catch (Exception e) {
                    e.printStackTrace(out);
                }
            }
        }
    }
}
