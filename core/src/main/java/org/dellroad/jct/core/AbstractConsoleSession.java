
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * Support superclass for {@link ConsoleSession} implementations.
 *
 * @param <O> associated owner type
 * @param <R> associated request type
 */
public abstract class AbstractConsoleSession<O, R extends ConsoleRequest<R>> implements ConsoleSession<O, R> {

    protected final O owner;
    protected final R request;

    private Thread executionThread;
    private boolean executed;

    /**
     * Constructor.
     *
     * @param owner session owner
     * @param request associated request
     * @throws IllegalArgumentException if either parameter is null
     */
    protected AbstractConsoleSession(O owner, R request) {
        if (owner == null)
            throw new IllegalArgumentException("null owner");
        if (request == null)
            throw new IllegalArgumentException("null request");
        this.owner = owner;
        this.request = request;
    }

// Session

    @Override
    public O getOwner() {
        return this.owner;
    }

    @Override
    public R getRequest() {
        return this.request;
    }

    @Override
    public int execute() throws InterruptedException {
        synchronized (this) {
            if (this.executed)
                throw new IllegalStateException("already executed");
            this.executed = true;
            this.executionThread = Thread.currentThread();
        }
        try {
            return this.doExecute();
        } finally {
            synchronized (this) {
                this.executionThread = null;
            }
        }
    }

    @Override
    public synchronized boolean interrupt() {
        return this.executionThread != null && this.doInterrupt(this.executionThread);
    }

// Subclass Methods

    /**
     * Execute this session.
     *
     * <p>
     * This method is invoked by {@link #execute} to do the actual work.
     *
     * @return zero if successful, non-zero error code if an error occurred
     * @throws InterruptedException if the current thread is interrupted
     */
    protected abstract int doExecute() throws InterruptedException;

    /**
     * Interrupt execution.
     *
     * <p>
     * This method is invoked by {@link #interrupt} to actually do whatever is needed to interrupt execution,
     * but only if {@link #execute} is still executing.
     *
     * <p>
     * The implementation in {@link AbstractConsoleSession} just invokes {@link Thread#interrupt} on the given thread.
     *
     * <p>
     * NOTE: When this method is invoked, the current instance will be locked.
     *
     * @param thread the thread currently executing {@link #execute}
     * @return true if execution was interrupted, false if it was not possible to interrupt execution
     * @throws IllegalArgumentException if {@code thread} is null
     */
    protected boolean doInterrupt(Thread thread) {
        if (thread == null)
            throw new IllegalStateException("null thread");
        thread.interrupt();
        return true;
    }
}
