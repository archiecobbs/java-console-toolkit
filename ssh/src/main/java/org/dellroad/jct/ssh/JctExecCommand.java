
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.dellroad.jct.core.Exec;
import org.dellroad.jct.core.ExecSession;
import org.dellroad.jct.core.simple.SimpleExecRequest;
import org.dellroad.jct.core.util.CrNlPrintStream;

public class JctExecCommand extends AbstractCommand<Exec, ExecSession> {

    private final String command;

    public JctExecCommand(Exec exec, ChannelSession channel, String command) {
        super(exec, channel);
        if (command == null)
            throw new IllegalArgumentException("null command");
        this.command = command;
    }

// AbstractCommand

    @Override
    protected ExecSession createSession() throws IOException {

        // Create printable output streams using the SSH client's character encoding
        final PrintStream pout = CrNlPrintStream.of(this.out, this.charset);
        final PrintStream perr = CrNlPrintStream.of(this.err, this.charset);

        // Execute command
        return this.factory.newExecSession(new SimpleExecRequest(this.in, pout, perr, this.env.getEnv(), this.command));
    }

    @Override
    protected void handleChannelSignal(Channel channel, Signal signal) {
        super.handleChannelSignal(channel, signal);
        this.log.debug("rec'd channel signal {}", signal.name());
    }
}
