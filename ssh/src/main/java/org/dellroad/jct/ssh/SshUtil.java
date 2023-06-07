
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh;

import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;

/**
 * Utility methods relating to Apache MINA SSHD.
 */
public final class SshUtil {

    private static final String ENV_LC_ALL = "LC_ALL";
    private static final String ENV_LC_CTYPE = "LC_CTYPE";
    private static final String ENV_LANG = "LANG";

    private static final Pattern LC_TYPE_PATTERN = Pattern.compile("(?:\\p{Alpha}{2}_\\p{Alpha}{2}\\.)?([^@]+)(?:@.*)?");

    private static final EnumMap<Signal, Terminal.Signal> CHANNEL_TO_TERMINAL_SIGNAL_MAP = new EnumMap<>(Signal.class);
    private static final EnumMap<Terminal.Signal, Signal> TERMINAL_TO_CHANNEL_SIGNAL_MAP = new EnumMap<>(Terminal.Signal.class);
    static {
        CHANNEL_TO_TERMINAL_SIGNAL_MAP.put(Signal.INT, Terminal.Signal.INT);
        CHANNEL_TO_TERMINAL_SIGNAL_MAP.put(Signal.QUIT, Terminal.Signal.QUIT);
        CHANNEL_TO_TERMINAL_SIGNAL_MAP.put(Signal.TSTP, Terminal.Signal.TSTP);
        CHANNEL_TO_TERMINAL_SIGNAL_MAP.put(Signal.CONT, Terminal.Signal.CONT);
        CHANNEL_TO_TERMINAL_SIGNAL_MAP.put(Signal.WINCH, Terminal.Signal.WINCH);
        CHANNEL_TO_TERMINAL_SIGNAL_MAP.forEach((key, value) -> TERMINAL_TO_CHANNEL_SIGNAL_MAP.put(value, key));
    }

    private static final Map<PtyMode, Enum<?>> ATTR_MAP = new EnumMap<>(PtyMode.class);
    static {

        // Control characters
        ATTR_MAP.put(PtyMode.VINTR,     Attributes.ControlChar.VINTR);
        ATTR_MAP.put(PtyMode.VQUIT,     Attributes.ControlChar.VQUIT);
        ATTR_MAP.put(PtyMode.VERASE,    Attributes.ControlChar.VERASE);
        ATTR_MAP.put(PtyMode.VKILL,     Attributes.ControlChar.VKILL);
        ATTR_MAP.put(PtyMode.VEOF,      Attributes.ControlChar.VEOF);
        ATTR_MAP.put(PtyMode.VEOL,      Attributes.ControlChar.VEOL);
        ATTR_MAP.put(PtyMode.VEOL2,     Attributes.ControlChar.VEOL2);
        ATTR_MAP.put(PtyMode.VSTART,    Attributes.ControlChar.VSTART);
        ATTR_MAP.put(PtyMode.VSTOP,     Attributes.ControlChar.VSTOP);
        ATTR_MAP.put(PtyMode.VSUSP,     Attributes.ControlChar.VSUSP);
        ATTR_MAP.put(PtyMode.VDSUSP,    Attributes.ControlChar.VDSUSP);
        ATTR_MAP.put(PtyMode.VREPRINT,  Attributes.ControlChar.VREPRINT);
        ATTR_MAP.put(PtyMode.VWERASE,   Attributes.ControlChar.VWERASE);
        ATTR_MAP.put(PtyMode.VLNEXT,    Attributes.ControlChar.VLNEXT);
      //ATTR_MAP.put(PtyMode.VFLUSH,    Attributes.ControlChar.VFLUSH);
      //ATTR_MAP.put(PtyMode.VSWTCH,    Attributes.ControlChar.VSWTCH);
        ATTR_MAP.put(PtyMode.VSTATUS,   Attributes.ControlChar.VSTATUS);
        ATTR_MAP.put(PtyMode.VDISCARD,  Attributes.ControlChar.VDISCARD);

        // Input flags
        ATTR_MAP.put(PtyMode.ICRNL,     Attributes.InputFlag.ICRNL);
        ATTR_MAP.put(PtyMode.INLCR,     Attributes.InputFlag.INLCR);
        ATTR_MAP.put(PtyMode.IGNCR,     Attributes.InputFlag.IGNCR);

        // Output flags
        ATTR_MAP.put(PtyMode.OCRNL,     Attributes.OutputFlag.OCRNL);
        ATTR_MAP.put(PtyMode.ONLCR,     Attributes.OutputFlag.ONLCR);
        ATTR_MAP.put(PtyMode.ONLRET,    Attributes.OutputFlag.ONLRET);
        ATTR_MAP.put(PtyMode.OPOST,     Attributes.OutputFlag.OPOST);

        // Local flags
        ATTR_MAP.put(PtyMode.ECHO,      Attributes.LocalFlag.ECHO);
        ATTR_MAP.put(PtyMode.ICANON,    Attributes.LocalFlag.ICANON);
        ATTR_MAP.put(PtyMode.ISIG,      Attributes.LocalFlag.ISIG);
    }

    private SshUtil() {
    }

    /**
     * Map SSH channel signal to the corresponding {@link Terminal} signal, if able.
     *
     * @param signal SSH channel signal
     * @return corresponding {@link Terminal} signal, if known
     * @throws IllegalArgumentException if {@code signal} is null
     */
    public static Optional<Terminal.Signal> mapSignalToTerminal(Signal signal) {
        if (signal == null)
            throw new IllegalArgumentException("null signal");
        return Optional.of(signal).map(CHANNEL_TO_TERMINAL_SIGNAL_MAP::get);
    }

    /**
     * Map {@link Terminal} signal to the corresponding SSH channel signal.
     *
     * @param signal {@link Terminal} signal
     * @return corresponding SSH channel signal, if known
     * @throws IllegalArgumentException if {@code signal} is null
     */
    public static Optional<Signal> mapTerminalToSignal(Terminal.Signal signal) {
        if (signal == null)
            throw new IllegalArgumentException("null signal");
        return Optional.of(signal).map(TERMINAL_TO_CHANNEL_SIGNAL_MAP::get);
    }

    /**
     * Attempt to infer the character encoding associated with an SSH connection.
     *
     * @param env SSH environment
     * @return character encoding, if known
     * @throws IllegalArgumentException if {@code env} is null
     */
    public static Optional<Charset> inferCharacterEncoding(Environment env) {
        return Stream.of(ENV_LC_ALL, ENV_LC_CTYPE, ENV_LANG)
          .map(env.getEnv()::get)
          .filter(Objects::nonNull)
          .map(LC_TYPE_PATTERN::matcher)
          .filter(Matcher::matches)
          .map(matcher -> matcher.group(1))
          .map(name -> {
            try {
                return Charset.forName(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
          })
          .filter(Objects::nonNull)
          .findFirst();
    }

    /**
     * Attempt to infer the locale associated with an SSH connection.
     *
     * @param env SSH environment
     * @return local, if known
     * @throws IllegalArgumentException if {@code env} is null
     */
    public static Optional<Locale> inferLocale(Environment env) {
        return Stream.of(ENV_LC_ALL, ENV_LC_CTYPE, ENV_LANG)
          .map(env.getEnv()::get)
          .filter(Objects::nonNull)
          .map(Locale::new)
          .findFirst();
    }

    /**
     * Update {@link Terminal} attributes based on the given SSH connection.
     *
     * @param attr terminal attributes
     * @param env SSH connection info
     * @throws IllegalArgumentException if either parameter is null
     */
    public static void updateAttributesFromEnvironment(Attributes attr, Environment env) {
        if (attr == null)
            throw new IllegalArgumentException("null attr");
        if (env == null)
            throw new IllegalArgumentException("null env");
        env.getPtyModes().forEach((mode, value) -> {
            final Enum<?> attrKey = ATTR_MAP.get(mode);
            if (attrKey == null)
                return;
            if (attrKey instanceof Attributes.ControlChar)
                attr.setControlChar((Attributes.ControlChar)attrKey, value);
            else if (attrKey instanceof Attributes.InputFlag)
                attr.setInputFlag((Attributes.InputFlag)attrKey, value != 0);
            else if (attrKey instanceof Attributes.OutputFlag)
                attr.setOutputFlag((Attributes.OutputFlag)attrKey, value != 0);
            else if (attrKey instanceof Attributes.LocalFlag)
                attr.setLocalFlag((Attributes.LocalFlag)attrKey, value != 0);
            else
                throw new RuntimeException("internal error");
        });
    }

    /**
     * Update {@link Terminal} window size based on SSH environment variables.
     *
     * @param terminal the terminal to update
     * @param env SSH connection info
     * @return true if size was successfully updated
     * @throws IllegalArgumentException if either parameter is null
     */
    public static boolean updateSize(Terminal terminal, Environment env) {

        // Validate
        if (terminal == null)
            throw new IllegalArgumentException("null terminal");
        if (env == null)
            throw new IllegalArgumentException("null env");

        // Find and parse valid environment variables
        final String colsString = env.getEnv().get(Environment.ENV_COLUMNS);
        final String rowsString = env.getEnv().get(Environment.ENV_LINES);
        if (colsString == null || rowsString == null)
            return false;
        final int cols;
        final int rows;
        try {
            cols = Integer.parseInt(colsString, 10);
            rows = Integer.parseInt(rowsString, 10);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (cols <= 0 || rows <= 0)
            return false;

        // Update size
        terminal.setSize(new Size(cols, rows));
        return true;
    }
}
