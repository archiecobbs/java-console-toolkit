
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jdk.jshell.execution.LocalExecutionControl;
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
 * The associated {@link jdk.jshell.JShell} instance can be customized in two ways:
 * <ul>
 *  <li>Override {@link #createBuilder createBuilder()} to customize the {@link JavaShellToolBuilder}
 *      used to create the JShell.
 *  <li>Override {@link #modifyJShellParams modifyJShellParams()} to customize the flags and parameters
 *      passed to JShell itself (these are the same as accepted by the {@code jshell(1)} command line tool).
 *      By default, the parameters passed are the the parameters given on the command line.
 * </ul>
 *
 * <p>
 * During execution, instances make themselves available to the current thread via {@link #getCurrent}.
 */
public class JShellShellSession extends AbstractShellSession {

    private static final InheritableThreadLocal<JShellShellSession> CURRENT_SESSION = new InheritableThreadLocal<>();

    protected ClassLoader localContextClassLoader;

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
     * <p>
     * This value is stored in an {@link InheritableThreadLocal} initialized when the JShell tool is started.
     * As a result, it is accessible not only from the main JShell  loop but also from the separate snippet
     * execution threads created by {@link LocalExecutionControl}.
     *
     * @return session associated with the current thread, or null if not found
     */
    public static JShellShellSession getCurrent() {
        return CURRENT_SESSION.get();
    }

    /**
     * Configure a class loader to use with {@link LocalContextExecutionControlProvider} for local execution.
     *
     * @param loader class loader, or null for none
     * @see #modifyJShellParams
     */
    public void setLocalContextClassLoader(ClassLoader loader) {
        this.localContextClassLoader = loader;
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
        final JavaShellToolBuilder builder = this.createBuilder();
        final Terminal terminal = this.request.getTerminal();
        final Attributes attr = terminal.enterRawMode();
        final JShellShellSession previousSession = CURRENT_SESSION.get();
        final List<String> jshellParams = this.modifyJShellParams(this.request.getShellArguments());
        if (jshellParams == null)
            throw new IllegalArgumentException("null jshellParams");
        CURRENT_SESSION.set(this);
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousLoader = currentThread.getContextClassLoader();
        if (this.localContextClassLoader != null)
            currentThread.setContextClassLoader(this.localContextClassLoader);
        try {
            final String[] params = jshellParams.toArray(new String[0]);
            if (ConsoleUtil.getJavaVersion() >= 11) {
                try {
                    // return builder.start(jshellParams);
                    return (int)JavaShellToolBuilder.class.getMethod("start", String[].class).invoke(builder, (Object)params);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("unexpected error", e);
                }
            } else {
                builder.run(params);
                return 0;
            }
        } catch (Exception e) {
            this.out.println(String.format("Error: %s", e));
            return 1;
        } finally {
            CURRENT_SESSION.set(previousSession);
            currentThread.setContextClassLoader(previousLoader);
            terminal.setAttributes(attr);
        }
    }

// Subclass Methods

    /**
     * Create and configure the JShell builder for this new session.
     *
     * @return new builder
     */
    protected JavaShellToolBuilder createBuilder() {
        final JavaShellToolBuilder builder = JavaShellToolBuilder.builder();
        if (ConsoleUtil.getJavaVersion() >= 17) {
            try {
                // builder.interactiveTerminal(true);
                JavaShellToolBuilder.class.getMethod("interactiveTerminal", Boolean.TYPE).invoke(builder, true);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("unexpected error", e);
            }
        }
        builder.env(this.request.getEnvironment());
        //builder.locale(???);
        builder.in(this.in, this.in);
        builder.out(this.out);
        return builder;
    }

    /**
     * Generate a list of command line flags and parameters to be passed to the JShell tool,
     * given the arguments given on the shell command line for this command.
     *
     * <p>
     * If this method is overridden to add or change this command's flags and/or parameters,
     * then the full constructor taking customized help detail should be used to describe the
     * new usage.
     *
     * <p>
     * The implementation in {@link JShellShellSession} just returns the list unmodified unless
     * a {@linkplain #setLocalContextClassLoader local context class loader} has been configured,
     * in which case the list is copied, modified by {@link LocalContextExecutionControlProvider#modifyJShellFlags},
     * and then returned.
     *
     * @param params parameters given to the shell command line
     * @return flags and parameters for JShell
     * @throws IllegalArgumentException if {@code commandLineParams} is null
     * @see #setLocalContextClassLoader
     */
    protected List<String> modifyJShellParams(List<String> params) {
        if (params == null)
            throw new IllegalArgumentException("null params");
        if (this.localContextClassLoader != null) {
            params = new ArrayList<>(params);
            LocalContextExecutionControlProvider.modifyJShellFlags(this.localContextClassLoader, params);
        }
        return params;
    }
}
