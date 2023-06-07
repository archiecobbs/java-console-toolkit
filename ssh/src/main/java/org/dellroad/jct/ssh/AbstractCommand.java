
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.dellroad.jct.core.JctConsole;
import org.dellroad.jct.core.JctSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCommand<S extends JctSession> implements Command {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final JctConsole console;
    protected final ChannelSession channel;

    protected InputStream in;
    protected OutputStream out;
    protected OutputStream err;
    protected ExitCallback exitCallback;

    protected Environment env;
    protected Charset charset;
    protected Locale locale;
    protected S session;

// Constructors

    protected AbstractCommand(JctConsole console, ChannelSession channel) {
        if (console == null)
            throw new IllegalArgumentException("null console");
        if (channel == null)
            throw new IllegalArgumentException("null channel");
        this.console = console;
        this.channel = channel;
    }

// Command

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        this.exitCallback = exitCallback;
    }

    @Override
    public final void start(ChannelSession channel, Environment env) throws IOException {

        // Save environment
        this.env = env;

        // Catch signals coming in from the SSH client
        this.env.addSignalListener(this::handleChannelSignal);

        // Determine character encoding and locale
        this.charset = SshUtil.inferCharacterEncoding(this.env).orElse(StandardCharsets.UTF_8);
        this.locale = SshUtil.inferLocale(this.env).orElseGet(Locale::getDefault);

        // Start command and wait for it to complete
        if ((this.session = this.start()) == null)
            throw new IOException("null session returned from " + this.getClass().getName() + ".start()");
/*
        final Future<?> future = this.session.getCompletionFuture();
        try {
            future.get();
        } catch (CancellationException e) {
            // ignore
        } catch (InterruptedException e) {
            CrNlPrintStream.of(this.err).println("Interrupted");
            future.cancel(true);
        } catch (ExecutionException e) {
            this.setExitValue(1);
            e.getCause().printStackTrace(CrNlPrintStream.of(this.err));
        }
*/
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        if (this.session != null) {
            this.session.close();
            if (this.exitCallback != null)
                this.exitCallback.onExit(this.session.getExitValue(), false);
        }
        for (Closeable c : new Closeable[] { this.err, this.out, this.in }) {
            try {
                if (c != null)
                    c.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

// Subclass Hooks

    /**
     * Start the (asynchronous) execution of this command.
     *
     * @return newly created session
     * @throws IOException if an I/O error occurs
     */
    protected abstract S start() throws IOException;

    /**
     * Handle an incoming signal from the SSH client terminal.
     *
     * <p>
     * The implementation in {@link AbstractCommand} does nothing.
     *
     * @param channel SSH channel
     * @param signal the signal received
     */
    protected void handleChannelSignal(Channel channel, Signal signal) {
    }
}
