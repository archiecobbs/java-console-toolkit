
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.Map;

/**
 * Represents a request to create a new {@link ConsoleSession} of some kind.
 *
 * @param <R> subclass type
 */
public interface ConsoleRequest<R extends ConsoleRequest<R>> {

    /**
     * Get the session's environment variables.
     *
     * @return environment variables; may be immutable
     */
    Map<String, String> getEnvironment();
}
