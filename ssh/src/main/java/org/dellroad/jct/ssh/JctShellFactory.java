
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.shell.ShellFactory;
import org.dellroad.jct.core.Shell;

/**
 * An Apache MINA SSHD {@link ShellFactory} that connects to a Java Console Toolkit {@link Shell} instance.
 */
public class JctShellFactory implements ShellFactory {

    protected final Shell shell;

    /**
     * Constructor.
     *
     * @param shell the underlying shell
     * @throws IllegalArgumentException if {@code shell} is null
     */
    public JctShellFactory(Shell shell) {
        if (shell == null)
            throw new IllegalArgumentException("null shell");
        this.shell = shell;
    }

// ShellFactory

    @Override
    public JctShellCommand createShell(ChannelSession channel) {
        return new JctShellCommand(this.shell, channel);
    }
}
