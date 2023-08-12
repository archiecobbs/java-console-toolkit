
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.util.Map;

import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

/**
 * Same as {@link LocalExecutionControlProvider} but configures the {@link LocalExecutionControl}s
 * that it creates with a {@link MemoryLoaderDelegate}.
 *
 * <p>
 * Can be chosen via the name {@value #NAME}.
 */
public class LocalContextExecutionControlProvider implements ExecutionControlProvider {

    public static final String NAME = "localContext";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, String> defaultParameters() {
        return ExecutionControlProvider.super.defaultParameters();
    }

    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> params) {

        // Create our class loader
        final MemoryClassLoader memoryLoader = this.createMemoryClassLoader();

        // Set our class loader as the context loader for the current thread.
        // Note: this action gets undone in JShellShellSession.doExecute().
        Thread.currentThread().setContextClassLoader(memoryLoader);

        // Create our delegate thingie
        final MemoryLoaderDelegate delegate = this.createMemoryLoaderDelegate(memoryLoader);

        // Create local ExecutionControl using delegate
        return new LocalExecutionControl(delegate);
    }

// Subclass Methods

    protected MemoryLoaderDelegate createMemoryLoaderDelegate(MemoryClassLoader memoryLoader) {
        return new MemoryLoaderDelegate(memoryLoader);
    }

    protected MemoryClassLoader createMemoryClassLoader() {
        return new MemoryClassLoader();
    }
}
