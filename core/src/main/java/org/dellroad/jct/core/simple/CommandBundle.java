
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A collection of {@link SimpleCommand}s keyed by name.
 */
public interface CommandBundle extends SortedMap<String, SimpleCommand> {

    /**
     * Get a one line description of this bundle.
     *
     * @return bundle description
     */
    String getDescription();

    /**
     * Scan the classpath for {@link CommandBundle} implementations, instantiate, and return them.
     *
     * <p>
     * This method uses the current thread's context class loader.
     *
     * @return stream of auto-generated {@link CommandBundle}'s
     * @see ServiceLoader
     */
    static Stream<CommandBundle> scanAndGenerate() {
        return CommandBundle.scanAndGenerate(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Scan the classpath for {@link CommandBundle} implementations and instantiate and return them.
     *
     * @param loader class loader, or null for the system class loader
     * @return stream of auto-generated {@link CommandBundle}'s
     * @see ServiceLoader
     */
    static Stream<CommandBundle> scanAndGenerate(ClassLoader loader) {
        return StreamSupport.stream(ServiceLoader.load(CommandBundle.class, loader).spliterator(), false);
    }
}
