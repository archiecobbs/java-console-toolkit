
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * A service that provides {@link SimpleCommand}s to be used by a console, intended to be loaded using {@link ServiceLoader}.
 *
 * @see CommandRegistry#autoGenerate
 * @see ServiceLoader
 */
public interface CommandProvider {

    /**
     * Create {@link SimpleCommand}s and return them keyed by command name.
     *
     * @return stream of commands keyed by name, or null for none
     */
    Stream<? extends Map.Entry<String, ? extends SimpleCommand>> generateSimpleCommands();
}
