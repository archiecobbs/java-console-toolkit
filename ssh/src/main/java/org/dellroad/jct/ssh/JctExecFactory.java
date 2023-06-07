
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.IOException;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.CommandFactory;
import org.dellroad.jct.core.JctConsole;

/**
 * An Apache MINA SSHD {@link CommandFactory} that connects to a {@link JctConsole}.
 */
public class JctExecFactory implements CommandFactory {

    protected final JctConsole console;

    /**
     * Constructor.
     *
     * @param console the underlying console
     * @throws IllegalArgumentException if {@code console} is null
     */
    public JctExecFactory(JctConsole console) {
        if (console == null)
            throw new IllegalArgumentException("null console");
        this.console = console;
    }

// CommandFactory

    @Override
    public JctExecCommand createCommand(ChannelSession channel, String command) throws IOException {
        return new JctExecCommand(this.console, channel, command);
    }
}
