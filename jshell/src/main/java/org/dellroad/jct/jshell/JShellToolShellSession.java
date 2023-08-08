
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.PrintStream;

import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.AbstractShellSession;
import org.dellroad.jct.core.Shell;
import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.util.ConsoleUtil;
import org.jline.terminal.Terminal;

/**
 * A {@link Shell} session that fires up the JShell tool using {@link JavaShellToolBuilder}.
 */
public class JShellToolShellSession extends AbstractShellSession {

    /**
     * Constructor.
     *
     * @param shell owning shell
     * @param request shell request
     * @throws IllegalArgumentException if either parameter is null
     */
    public JShellToolShellSession(Shell shell, ShellRequest request) {
        super(shell, request);
    }

// Subclass Methods

    // JShell closes the output on exit, so we prevent that here
    @Override
    protected PrintStream buildOutputStream(Terminal terminal) {
        return ConsoleUtil.unclosable(super.buildOutputStream(terminal));
    }

    @Override
    protected int doExecute() throws InterruptedException {

        // Configure tool builder
        final JavaShellToolBuilder builder = this.createBuilder(request);

        // Execute tool
        final int exitValue;
        try {
            exitValue = builder.start(this.request.getShellArguments().toArray(new String[0]));
        } catch (Exception e) {
            this.out.println(String.format("Error: %s", e));
            return 1;
        }

        // Done
        return exitValue;
    }

    /**
     * Create and configure the tool builder.
     *
     * @param request session request
     * @return new builder
     */
    protected JavaShellToolBuilder createBuilder(ShellRequest request) {
        JavaShellToolBuilder builder = JavaShellToolBuilder.builder();
        builder.interactiveTerminal(true);
        builder.env(this.request.getEnvironment());
        //builder.locale(this.request.getTerminal().getLocale());  TODO
        builder.in(this.in, this.in);
        builder.out(this.out);
        return builder;
    }
}
