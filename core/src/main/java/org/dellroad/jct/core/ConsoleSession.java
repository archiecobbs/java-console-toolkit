
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A session associated with a console component.
 *
 * <p>
 * {@link ConsoleSession}s are created by console components from {@link ConsoleRequest}s and then {@linkplain #execute executed}
 * synchronously. During execution, {@link #interrupt} may be invoked (by another thread) to signal that the execution
 * should be interrupted.
 *
 * <p>
 * {@link ConsoleSession}s are not reusable.
 *
 * @param <O> associated owner type
 * @param <R> associated request type
 */
public interface ConsoleSession<O, R extends ConsoleRequest<R>> {

    /**
     * Get the owner, i.e., the console component that created this session.
     *
     * @return owning component
     */
    O getOwner();

    /**
     * Get the request from which this session was created.
     *
     * @return original request
     */
    R getRequest();

    /**
     * Get this session's input stream.
     *
     * @return session input
     */
    InputStream getInputStream();

    /**
     * Get this session's output stream.
     *
     * @return session output
     */
    PrintStream getOutputStream();

    /**
     * Get this session's error output stream.
     *
     * @return session error output
     */
    PrintStream getErrorStream();

    /**
     * Execute this session synchronously in the current thread.
     *
     * <p>
     * Instances should ensure any associated resources are cleaned up when this method
     * returns, whether normally or via thrown exception.
     *
     * @return zero if successful, non-zero error code if an error occurred
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
     * {@link InterruptedException} or at least returns immediately. In general, it should have
     * roughly the same effect as one would expect when pressing Control-C on a controlling terminal.
     *
     * @return true if execution was interrupted, false if it was not possible to interrupt execution
     */
    boolean interrupt();
}
