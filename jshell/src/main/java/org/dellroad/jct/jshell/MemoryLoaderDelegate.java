
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import jdk.jshell.execution.LoaderDelegate;
import jdk.jshell.spi.ExecutionControl.ClassBytecodes;
import jdk.jshell.spi.ExecutionControl.ClassInstallException;
import jdk.jshell.spi.ExecutionControl.EngineTerminationException;
import jdk.jshell.spi.ExecutionControl.InternalException;

import org.dellroad.stuff.java.MemoryClassLoader;

/**
 * A JShell {@link LoaderDelegate} that uses a {@link MemoryClassLoader}.
 */
public class MemoryLoaderDelegate implements LoaderDelegate {

    private final HashMap<String, Class<?>> classMap = new HashMap<>();
    private final MemoryClassLoader loader;

// Constructors

    /**
     * Default constructor.
     *
     * <p>
     * Uses the current thread's context loader as the parent loader for a newly created {@link MemoryClassLoader}.
     */
    public MemoryLoaderDelegate() {
        this(new MemoryClassLoader());
    }

    /**
     * Primary constructor.
     *
     * @param loader associated class loader
     * @throws IllegalArgumentException if {@code loader} is null
     */
    public MemoryLoaderDelegate(MemoryClassLoader loader) {
        if (loader == null)
            throw new IllegalArgumentException("null loader");
        this.loader = loader;
    }

    /**
     * Get the {@link MemoryClassLoader} associated with this instance.
     *
     * @return associated class loader
     */
    public MemoryClassLoader getClassLoader() {
        return this.loader;
    }

// LoaderDelegate

    @Override
    public void addToClasspath(String path) throws EngineTerminationException, InternalException {
        if (path == null)
            throw new IllegalArgumentException("null path");
        try {
            for (String elem : path.split(File.pathSeparator)) {
                if (elem.isEmpty())
                    continue;
                this.loader.addURL(new File(elem).toURI().toURL());
            }
        } catch (Exception e) {
            throw this.wrapCause(e, new InternalException(e.getMessage()));
        }
    }

    @Override
    public void classesRedefined(ClassBytecodes[] classes) {
        if (classes == null)
            throw new IllegalArgumentException("null classes");
        for (ClassBytecodes c : classes)
            this.loader.putClass(c.name(), c.bytecodes());
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        if (className == null)
            throw new IllegalArgumentException("null className");
        final Class<?> cl = this.classMap.get(className);
        if (cl == null)
            throw new ClassNotFoundException("class \"" + className + "\" not found");
        return cl;
    }

    @Override
    public void load(ClassBytecodes[] classes) throws ClassInstallException, EngineTerminationException {
        if (classes == null)
            throw new IllegalArgumentException("null classes");
        int numLoaded = 0;
        try {
            for (ClassBytecodes c : classes)
                this.loader.putClass(c.name(), c.bytecodes());
            for (ClassBytecodes c : classes) {
                final Class<?> cl = this.loader.loadClass(c.name());
                this.classMap.put(c.name(), cl);
                numLoaded++;
                cl.getDeclaredMethods();
            }
        } catch (Throwable t) {
            final boolean[] loaded = new boolean[classes.length];
            Arrays.fill(loaded, 0, numLoaded, true);
            throw this.wrapCause(t, new ClassInstallException(t.getMessage(), loaded));
        }
    }

// Internal Methods

    private <T extends Throwable, T2 extends Throwable> T2 wrapCause(T t, T2 t2) {
        t2.initCause(t);
        return t2;
    }
}
