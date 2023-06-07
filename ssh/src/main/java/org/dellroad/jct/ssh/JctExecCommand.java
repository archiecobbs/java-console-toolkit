
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.JctExecSession;
import org.dellroad.jct.core.SimpleExecRequest;

public class JctExecCommand extends AbstractCommand<JctExecSession> {

    private final String command;

    public JctExecCommand(JctConsole console, ChannelSession channel, String command) {
        super(console, channel);
        if (command == null)
            throw new IllegalArgumentException("null command");
        this.command = command;
    }

// AbstractCommand

    @Override
    protected JctExecSession createSession() throws IOException {

        // Create printable output streams using the SSH client's character encoding
        final PrintStream pout = CrNlPrintStream.of(this.out, this.charset);
        final PrintStream perr = CrNlPrintStream.of(this.err, this.charset);

        // Execute command
        return this.console.newExecSession(new SimpleExecRequest(this.in, pout, perr, this.env.getEnv(), this.command));
    }

    @Override
    protected void handleChannelSignal(Channel channel, Signal signal) {
        super.handleChannelSignal(channel, signal);
        this.log.debug("rec'd channel signal {}", signal.name());
    }
}
