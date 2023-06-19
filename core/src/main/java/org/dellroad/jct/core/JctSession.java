
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * A session associated with a {@link JctConsole}.
 */
public interface JctSession {

    /**
     * Execute this session in the current thread and return an integer result code when complete.
     *
     * <p>
     * Instances should ensure any associated resources are cleaned up when this method
     * returns, whether normally or via thrown exception.
     *
     * @return session exit value
     * @throws InterruptedException if execution is interrupted via {@link #interrupt}
     * @throws IllegalStateException if this method has already been invoked
     */
    int execute() throws InterruptedException;

    /**
     * Interrupt the execution of this session.
     *
     * <p>
     * This method no effect unless {@link #execute} has been invoked and not yet returned.
     *
     * <p>
     * How this method is handled is up to the session: in general, it should cause the execution
     * of {@link #execute} to be interrupted, such that {@link #execute} then either throws an
     * {@link InterruptedException} or returns an error code. In general, it should have roughly
     * the same effect as one would expect when pressing Control-C on a controlling terminal.
     *
     * @return true if execution was interrupted, false if it was not possible to interrupt execution
     */
    boolean interrupt();
}
