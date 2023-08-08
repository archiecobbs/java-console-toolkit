
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.IOException;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.CommandFactory;
import org.dellroad.jct.core.Exec;

/**
 * An Apache MINA SSHD {@link CommandFactory} that connects to a Java Console Toolkit {@link Exec} instance.
 */
public class JctExecFactory implements CommandFactory {

    protected final Exec exec;

    /**
     * Constructor.
     *
     * @param exec the underlying {@link Exec} instance
     * @throws IllegalArgumentException if {@code exec} is null
     */
    public JctExecFactory(Exec exec) {
        if (exec == null)
            throw new IllegalArgumentException("null exec");
        this.exec = exec;
    }

// CommandFactory

    @Override
    public JctExecCommand createCommand(ChannelSession channel, String command) throws IOException {
        return new JctExecCommand(this.exec, channel, command);
    }
}
