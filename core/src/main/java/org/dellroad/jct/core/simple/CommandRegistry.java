
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.SortedMap;

/**
 * A registry of {@link SimpleCommand}s.
 */
public interface CommandRegistry {

    /**
     * Get commands sorted by name.
     *
     * @return map of command name to command; this map may be immutable
     */
    SortedMap<String, SimpleCommand> getCommands();
}
