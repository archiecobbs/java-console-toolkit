
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.PrintStream;

/**
 * A {@link ConsoleSession} associated with an interactive shell.
 *
 * <p>
 * Note that shell sessions do not have distinct output and error streams. Instead, there is
 * only one output stream, namely, the stream that ultimately gets displayed on the user's terminal.
 * This stream is used for both normal and error output. However, it would be possible for a session
 * to do clever things like displaying normal and error output in different colors, etc.
 */
public interface ShellSession extends ConsoleSession<Shell, ShellRequest> {

    /**
     * Signal to the shell that it should exit with the specified exit value.
     *
     * <p>
     * Whether the {@code exitValue} is meaningful and what to do with it (if anything) is up to the shell.
     *
     * @param exitValue integer exit value
     * @return true if successful, false if setting exit value is unsupported or otherwise not possible
     */
    boolean setExitValue(int exitValue);

    /**
     * Get the current exit value, if any.
     *
     * @return exit value, or zero if none set or not available
     */
    int getExitValue();

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation in {@link ShellSession} just returns {@link #getOutputStream this.getOutputStream()}.
     */
    @Override
    default PrintStream getErrorStream() {
        return this.getOutputStream();
    }
}
