
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * A session associated with a {@link JctConsole}.
 */
public interface JctSession extends Activity {

    /**
     * Get the integer exit value result from this session once completed.
     *
     * <p>
     * This method is used by console inputs that expect an exit value, e.g., SSH server.
     *
     * <p>
     * The return value from this method is valid once this session is no longer {@linkplain #isActive active}.
     *
     * <p>
     * The default implementation in {@link JctSession} always returns zero.
     *
     * @return session exit value
     */
    default int getExitValue() {
        return 0;
    }
}
