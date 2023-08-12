
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import jdk.jshell.tool.JavaShellToolBuilder;

import org.dellroad.jct.core.Shell;
import org.dellroad.jct.core.ShellRequest;

/**
 * A {@link Shell} wrapper around {@link jdk.jshell.JShell}.
 *
 * <p>
 * See {@link JShellCommand} for details.
 */
public class JShellShell implements Shell {

    @Override
    public JShellShellSession newShellSession(ShellRequest request) {
        return new JShellShellSession(this, request);
    }

// Subclass Methods

    /**
     * Create and configure the JShell builder.
     *
     * @param session new session
     * @return new builder
     */
    protected JavaShellToolBuilder createBuilder(JShellShellSession session) {
        final JavaShellToolBuilder builder = JavaShellToolBuilder.builder();
        builder.interactiveTerminal(true);
        builder.env(session.getRequest().getEnvironment());
        //builder.locale(???);
        builder.in(session.getInputStream(), session.getInputStream());
        builder.out(session.getOutputStream());
        return builder;
    }
}
