
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.shell.ShellFactory;
import org.dellroad.jct.core.JctConsole;

/**
 * An Apache MINA SSHD {@link ShellFactory} that connects to a {@link JctConsole}.
 */
public class JctShellFactory implements ShellFactory {

    protected final JctConsole console;

    /**
     * Constructor.
     *
     * @param console the underlying console
     * @throws IllegalArgumentException if {@code console} is null
     */
    public JctShellFactory(JctConsole console) {
        if (console == null)
            throw new IllegalArgumentException("null console");
        this.console = console;
    }

// ShellFactory

    @Override
    public JctShellCommand createShell(ChannelSession channel) {
        return new JctShellCommand(this.console, channel);
    }
}
