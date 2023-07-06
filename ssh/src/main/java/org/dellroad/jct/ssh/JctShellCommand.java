
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.IOException;

import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.JctShellSession;
import org.dellroad.jct.core.JctUtils;
import org.dellroad.jct.core.simple.SimpleShellRequest;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class JctShellCommand extends AbstractCommand<JctShellSession> {

    private volatile Terminal terminal;
    private volatile JctShellSession session;

    public JctShellCommand(JctConsole console, ChannelSession channel) {
        super(console, channel);
    }

// AbstractCommand

    @Override
    protected JctShellSession createSession() throws IOException {

        // Sanity check
        if (this.terminal != null || this.session != null)
            throw new IllegalStateException("session already created");

        // Build terminal
        TerminalBuilder builder = TerminalBuilder.builder()
          .name("ssh")
          .system(false)
          .encoding(this.charset)
          .signalHandler(JctUtils.interrruptHandler(() -> this.session, Terminal.SignalHandler.SIG_DFL))
          .streams(this.in, this.out);
        final String term = this.env.getEnv().get(Environment.ENV_TERM);
        if (term != null)
            builder = builder.type(term);
        this.terminal = builder.build();

        // Configure it
        final Attributes attrs = this.terminal.getAttributes();
        SshUtil.updateAttributesFromEnvironment(attrs, this.env);
        this.terminal.setAttributes(attrs);
        SshUtil.updateSize(this.terminal, this.env);

        // Create shell session
        this.session = this.console.newShellSession(new SimpleShellRequest(this.terminal, this.env.getEnv()));

        // Return shell session
        return this.session;
    }

    @Override
    protected void handleChannelSignal(Channel channel, Signal signal) {
        super.handleChannelSignal(channel, signal);
//        this.log.debug("rec'd channel signal {}", signal.name());
        switch (signal) {
        case WINCH:
            SshUtil.updateSize(this.terminal, this.env);
            break;
        default:
            break;
        }
//        SshUtil.mapSignalToTerminal(signal)
//          .ifPresent(this.terminal::raise);
    }

// Command

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        try {
            this.terminal.flush();
            try {
                this.terminal.close();
            } catch (IOException e) {
                // ignore
            }
        } finally {
            super.destroy(channel);
        }
    }
}
