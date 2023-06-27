
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A session associated with a {@link JctConsole}.
 *
 * <p>
 * A {@link JctSession} is not reusable.
 */
public interface JctSession {

    /**
     * Get the console with which this session is associated.
     *
     * @return associated console
     */
    JctConsole getConsole();

    /**
     * Get the source for standard input.
     *
     * @return standard input stream
     */
    InputStream getInputStream();

    /**
     * Get the destination for standard output.
     *
     * @return standard output stream
     */
    PrintStream getOutputStream();

    /**
     * Get the destination for standard error.
     *
     * @return standard error stream
     */
    PrintStream getErrorStream();

    /**
     * Execute this session in the current thread.
     *
     * <p>
     * Instances should ensure any associated resources are cleaned up when this method
     * returns, whether normally or via thrown exception.
     *
     * @return true if successful, false if an error occurred
     * @throws InterruptedException if execution is interrupted via {@link #interrupt}
     * @throws IllegalStateException if this method has already been invoked
     */
    boolean execute() throws InterruptedException;

    /**
     * Interrupt the execution of this session.
     *
     * <p>
     * This method no effect unless {@link #execute} has been invoked and not yet returned.
     *
     * <p>
     * How this method is handled is up to the session: in general, it should cause the execution
     * of {@link #execute} to be interrupted, such that {@link #execute} then either throws an
     * {@link InterruptedException} or at least returns immediately. In general, it should have
     * roughly the same effect as one would expect when pressing Control-C on a controlling terminal.
     *
     * @return true if execution was interrupted, false if it was not possible to interrupt execution
     */
    boolean interrupt();
}
