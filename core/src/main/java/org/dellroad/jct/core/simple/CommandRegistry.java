
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.StreamSupport;

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

    /**
     * Build an instance by scanning the classpath for {@link CommandProvider} implementations
     * using the current thread's context class loader.
     *
     * @return auto-generated {@link CommandRegistry}
     */
    static CommandRegistry autoGenerate() {
        return CommandRegistry.autoGenerate(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Build an instance by scanning the classpath for {@link CommandProvider} implementations
     * using the supplied class loader.
     *
     * @param loader class loader, or null for the system class loader
     * @return auto-generated {@link CommandRegistry}
     */
    static CommandRegistry autoGenerate(ClassLoader loader) {
        final TreeMap<String, SimpleCommand> commandMap = new TreeMap<>();
        StreamSupport.stream(ServiceLoader.load(CommandProvider.class, loader).spliterator(), false)
          .flatMap(CommandProvider::generateSimpleCommands)
          .filter(entry -> entry != null && entry.getKey() != null && entry.getValue() != null)
          .forEach(entry -> commandMap.put(entry.getKey(), entry.getValue()));
        return () -> commandMap;
    }
}
