
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.PrintStream;

import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.AbstractShellSession;
import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.util.ConsoleUtil;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;

/**
 * A {@link ShellSession} that builds and executes a {@link jdk.jshell.JShell} instance.
 *
 * <p>
 * See {@link JShellCommand} for details.
 */
public class JShellShellSession extends AbstractShellSession {

    private static final InheritableThreadLocal<JShellShellSession> CURRENT_SESSION = new InheritableThreadLocal<>();

    /**
     * Constructor.
     *
     * @param shell owning shell
     * @param request shell request
     * @throws IllegalArgumentException if any parameter is null
     */
    public JShellShellSession(JShellShell shell, ShellRequest request) {
        super(shell, request);
    }

// Public Methods

    /**
     * Get the instance associated with the current thread.
     *
     * @return session associated with the current thread, or null if not found
     */
    public static JShellShellSession getCurrent() {
        return CURRENT_SESSION.get();
    }

// AbstractConsoleSession

    @Override
    public JShellShell getOwner() {
        return (JShellShell)super.getOwner();
    }

// AbstractShellSession

    // JShell closes the output on exit, so we prevent that here
    @Override
    protected PrintStream buildOutputStream(Terminal terminal) {
        return ConsoleUtil.unclosable(super.buildOutputStream(terminal));
    }

    @Override
    protected int doExecute() throws InterruptedException {
        final JavaShellToolBuilder builder = this.getOwner().createBuilder(this);
        final Terminal terminal = this.request.getTerminal();
        final Attributes attr = terminal.enterRawMode();
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousLoader = currentThread.getContextClassLoader();
        final JShellShellSession previousSession = CURRENT_SESSION.get();
        CURRENT_SESSION.set(this);
        try {
            return builder.start(this.request.getShellArguments().toArray(new String[0]));
        } catch (Exception e) {
            this.out.println(String.format("Error: %s", e));
            return 1;
        } finally {
            CURRENT_SESSION.set(previousSession);
            currentThread.setContextClassLoader(previousLoader);
            terminal.setAttributes(attr);
        }
    }
}
