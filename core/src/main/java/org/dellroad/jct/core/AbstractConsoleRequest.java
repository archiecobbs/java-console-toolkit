
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.Map;

public class AbstractConsoleRequest<R extends ConsoleRequest<R>> implements ConsoleRequest<R> {

    private final Map<String, String> env;

    /**
     * Constructor.
     *
     * @param env evironment map
     * @throws IllegalArgumentException if {@code env} is null
     */
    protected AbstractConsoleRequest(Map<String, String> env) {
        if (env == null)
            throw new IllegalArgumentException("null env");
        this.env = env;
    }

    @Override
    public Map<String, String> getEnvironment() {
        return this.env;
    }
}
