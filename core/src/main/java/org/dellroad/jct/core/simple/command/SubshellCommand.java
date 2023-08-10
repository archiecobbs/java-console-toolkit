
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import java.io.IOException;
import java.util.List;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.Shell;
import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.simple.AbstractSimpleCommand;
import org.dellroad.jct.core.simple.SimpleShellRequest;

/**
 * A command that fires up subshell sessions.
 *
 * <p>
 * This command only works within shell sessions.
 */
public class SubshellCommand extends AbstractSimpleCommand {

    protected final Shell shell;

    /**
     * Constructor.
     *
     * @param shell the subshell, which creates the underlying subshell sessions
     * @param usage usage string, or null if command takes no arguments
     * @param summary help summary
     * @param detail help detail
     * @throws IllegalArgumentException if {@code shell}, {@code summary} or {@code detail} is null
     */
    public SubshellCommand(Shell shell, String usage, String summary, String detail) {
        super(usage, summary, detail);
        if (shell == null)
            throw new IllegalArgumentException("null shell");
        this.shell = shell;
    }

// AbstractSimpleCommand

    @Override
    public int execute(ConsoleSession<?, ?> session0, String name, List<String> params) throws InterruptedException {

        // Sanity check
        if (!(session0 instanceof ShellSession)) {
            session0.getErrorStream().println(String.format("Error: the \"%s\" command only works within shell sessions", name));
            return 1;
        }
        final ShellSession session = (ShellSession)session0;

        // Create request
        final ShellRequest request = this.buildShellRequest(session, name, params);

        // Create subshell
        final ShellSession subSession;
        try {
            subSession = this.createSubshell(request);
        } catch (IOException e) {
            session.getErrorStream().println(String.format("Error: %s", e));
            return 1;
        }

        // Execute subshell
        return subSession.execute();
    }

    /**
     * Create the subshell session.
     *
     * @param request subshell session request
     * @return subshell session
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code request} is null
     */
    protected ShellSession createSubshell(ShellRequest request) throws IOException {
        if (request == null)
            throw new IllegalArgumentException("null request");
        return this.shell.newShellSession(request);
    }

    /**
     * Create the subshell request.
     *
     * @param session outer shell session
     * @param name command name
     * @param params command parameters
     * @return new shell request
     */
    protected ShellRequest buildShellRequest(ShellSession session, String name, List<String> params) {
        return new SimpleShellRequest(session.getRequest().getTerminal(), params, session.getRequest().getEnvironment());
    }
}
