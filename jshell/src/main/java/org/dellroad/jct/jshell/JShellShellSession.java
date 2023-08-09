
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.PrintStream;

import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.AbstractShellSession;
import org.dellroad.jct.core.Shell;
import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.util.ConsoleUtil;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;

/**
 * A {@link ShellSession} that builds and executes a {@link jdk.jshell.JShell} instance.
 *
 * <p>
 * The {@link jdk.jshell.JShell} instance can be customized in two ways:
 * <ul>
 *  <li>Overriding {@link #createBuilder createBuilder()} allows customization of the {@link JavaShellToolBuilder}.
 *  <li>The flags in {@link ShellRequest#getShellArguments} are passed to {@link JavaShellToolBuilder#start}.
 * </ul>
 * As an example of the latter, including {@code "--execution=local"} would configure a {@link LocalExecutionControlProvider},
 * which makes it possible to access objects in the current JVM.
 */
public class JShellShellSession extends AbstractShellSession {

    /**
     * Constructor.
     *
     * @param shell owning shell
     * @param request shell request
     * @throws IllegalArgumentException if any parameter is null
     */
    public JShellShellSession(Shell shell, ShellRequest request) {
        super(shell, request);
    }

// AbstractShellSession

    // JShell closes the output on exit, so we prevent that here
    @Override
    protected PrintStream buildOutputStream(Terminal terminal) {
        return ConsoleUtil.unclosable(super.buildOutputStream(terminal));
    }

    @Override
    protected int doExecute() throws InterruptedException {
        final JavaShellToolBuilder builder = this.createBuilder(this.request);
        final Terminal terminal = this.request.getTerminal();
        final Attributes attr = terminal.enterRawMode();
        try {
            return builder.start(this.request.getShellArguments().toArray(new String[0]));
        } catch (Exception e) {
            this.out.println(String.format("Error: %s", e));
            return 1;
        } finally {
            terminal.setAttributes(attr);
        }
    }

// Subclass Methods

    /**
     * Create and configure the JShell builder.
     *
     * @param request session request
     * @return new builder
     */
    protected JavaShellToolBuilder createBuilder(ShellRequest request) {
        final JavaShellToolBuilder builder = JavaShellToolBuilder.builder();
        builder.interactiveTerminal(true);
        builder.env(this.request.getEnvironment());
        //builder.locale(???);  TODO
        builder.in(this.in, this.in);
        builder.out(this.out);
        return builder;
    }
}
