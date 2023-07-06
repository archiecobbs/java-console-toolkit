
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.function.Supplier;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Utility routines.
 */
public final class JctUtils {

    private JctUtils() {
    }

    /**
     * Build a {@link Terminal} signal handler that {@link JctSession#interrupt interrupt()}'s a {@link JctSession}
     * when a {@link Terminal.Signal#INT} signal is received.
     *
     * <p>
     * This is needed when building a {@link Terminal} to make Control-C work in a shell session: pass the returned
     * handler to {@link TerminalBuilder#signalHandler TerminalBuilder.signalHandler()}.
     *
     * <p>
     * For example:
     * <blockquote><pre>
     *  Terminal terminal = TerminalBuilder.builder()
     *    .signalHandler(JctUtils.interruptHandler(this::getSession, Terminal.SignalHandler.SIG_DFL))
     *    // other terminal config...
     *    .build();
     * </pre></blockquote>
     *
     * @param sessionGetter finds the session that should be interrupted; this may return null to demure
     * @param fallthroughHandler handler for signals other than {@link Terminal.Signal#INT},
     *  or when {@code sessionGetter} returns null; this may be null to do nothing
     * @return new signal handler
     */
    public static Terminal.SignalHandler interrruptHandler(Supplier<? extends JctSession> sessionGetter,
      Terminal.SignalHandler fallthroughHandler) {
        return signal -> {
            if (signal.equals(Terminal.Signal.INT)) {
                final JctSession session = sessionGetter.get();
                if (session != null) {
                    session.interrupt();
                    return;
                }
            }
            if (fallthroughHandler != null)
                fallthroughHandler.handle(signal);
        };
    }
}
