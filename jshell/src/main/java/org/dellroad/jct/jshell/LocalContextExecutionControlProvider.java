
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import org.dellroad.stuff.java.MemoryClassLoader;

/**
 * Same as {@link LocalExecutionControlProvider}, but with additional support for handling certain
 * class path and class loading issues.
 *
 * <p>
 * When executing JShell locally, it is often desirable that any classes visible to the thread
 * that starts JShell should also be visible to any scripts and command inputs given to that JShell
 * instance.
 *
 * <p>
 * Unfortunately, this doesn't always happen automatically when using the standard {@code "local"}
 * execution provided by {@link LocalExecutionControl}.
 *
 * <p>
 * When executing JShell locally, there are two class path/loading issues to worry about:
 * <ul>
 *  <li>When JShell compiles source snippets, what classes are available on the "source class path"?
 *      That is, what class names can you refer to by name in your scripts or snippets?
 *  <li>When a compiled script or snippet is loaded as a Java class file by the execution engine, what
 *      classes are available on the "binary class path" when resolving symbolic references?
 * </ul>
 *
 * <p>
 * The standard {@link LocalExecutionControl} requires non-standard classes to be explicitly added
 * via the {@code --class-path} command line flag. Moreover, the {@link ClassLoader} that is uses delegates
 * to the system class loader, which means that in certain more complex class loading scenarios (for example,
 * when running as a servlet in the Tomcat web container), compiled snippets classes will fail to load
 * due to resolution errors.
 *
 * <p>
 * This class tries to workaround these issues as follows:
 * <ul>
 *  <li>To address the "binary class path", this class uses a {@link ClassLoader} that delegates
 *      to the current thread's context class loader instead of the system class loader. This should
 *      fix linking problems in complex class loading scenarios.
 *  <li>To address the "souce class path", this class introspects the current thread's context class
 *      loader and its parents (recursively), attempting to glean what's on the class path. This works
 *      for any {@link ClassLoader} that are instances of {@link URLClassLoader}. However, Java's standard
 *      application class loader is not, so items on the JVM application class path are missed by this
 *      strategy. To include the application class loader, hacky instrospection relying on illegal accesses
 *      is attempted (failures are silently ignored). To ensure these efforts succeed, the following flag
 *      must be added to JVM startup: {@code --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED}.
 *      Note there is also another ugly workaround, which is to write JShell code that only accesses classes
 *      on the application class path via reflection.
 * </ul>
 *
 * <p>
 * To utilize this class, include the flags returned by {@link #modifyJShellFlags modifyJShellFlags()}
 * as parameters to JShell startup.
 *
 * @see <a href="https://bugs.openjdk.org/browse/JDK-8314327">JDK-8314327</a>
 */
public class LocalContextExecutionControlProvider implements ExecutionControlProvider {

    public static final String NAME = "localContext";

// Public Methods

    /**
     * Modify a list of JShell tool command line flags to enable use of this class.
     *
     * @param loader loader to copy from, or null for the current thread's context class loader
     * @param flags modifiable list of command line flags
     * @throws IllegalArgumentException if {@code flags} is null
     */
    public static void modifyJShellFlags(ClassLoader loader, List<String> flags) {

        // Sanity check
        if (flags == null)
            throw new IllegalArgumentException("null flags");
        if (loader == null)
            loader = Thread.currentThread().getContextClassLoader();

        // Prepare new classpath
        final StringBuilder classPath = new StringBuilder();
        final String pathSeparator = System.getProperty("path.separator", ":");
        final Consumer<Object> pathAdder = obj -> {
            if (classPath.length() > 0)
                classPath.append(pathSeparator);
            classPath.append(obj.toString());
        };

        // Visit class loader hierarchy
        for ( ; loader != null; loader = loader.getParent()) {

            // Extract classpath URLs from this loader
            final URL[] urls;
            if (loader instanceof URLClassLoader)
                urls = ((URLClassLoader)loader).getURLs();
            else {
                try {
                    // Ugly hack; we are trying to do this: "urls = loader.ucp.getURLs();"
                    final Field field = loader.getClass().getDeclaredField("ucp");
                    field.setAccessible(true);
                    final Object ucp = field.get(loader);
                    final Method method = ucp.getClass().getMethod("getURLs");
                    urls = (URL[])method.invoke(ucp);
                } catch (ReflectiveOperationException | SecurityException e) {
                    continue;
                }
            }

            // Pass these URLs to JShell by adding to our "--class-path" flag
            for (URL url : urls) {
                final URI uri;
                try {
                    uri = url.toURI();
                } catch (URISyntaxException e) {
                    continue;
                }
                final File file;
                try {
                    file = Paths.get(uri).toFile();
                } catch (IllegalArgumentException | FileSystemNotFoundException e) {
                    continue;
                }
                pathAdder.accept(file);
            }
        }

        // Extract and consolidate any existing "--class-path" entries with ours;
        // earlier JDK versions don't allow more than one "--class-path" flag.
        int i = 0;
        while (i < flags.size()) {
            final String flag = flags.get(i);
            if (flag.startsWith("--class-path=")) {
                flags.remove(i);
                pathAdder.accept(flag.substring("--class-path=".length()));
            } else if (flag.equals("--class-path")) {
                flags.remove(i);
                if (i < flags.size()) {
                    pathAdder.accept(flags.get(i));
                    flags.remove(i);
                }
            } else
                i++;
        }

        // Add our flags
        flags.add("--execution");
        flags.add(NAME);
        if (classPath.length() > 0) {
            flags.add("--class-path");
            flags.add(classPath.toString());
        }
    }

// ExecutionControlProvider

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
