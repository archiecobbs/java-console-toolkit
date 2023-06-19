
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * Support superclass for {@link JctSession} implementations.
 */
public abstract class AbstractJctSession implements JctSession {

    private Thread executionThread;
    private boolean executed;

// JctSession

    @Override
    public final int execute() throws InterruptedException {
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
    public final synchronized boolean interrupt() {
        if (!this.executed || this.executionThread == null)
            return false;
        this.doInterrupt(this.executionThread);
        return true;
    }

// Subclass Methods

    /**
     * Execute this session.
     *
     * <p>
     * This method is invoked by {@link #execute} to do the actual work.
     *
     * @return session exit value
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
     * The implementation in {@link AbstractJctSession} just invokes {@link Thread#interrupt} on the given thread.
     *
     * <p>
     * NOTE: When this method is invoked, the current instance will be locked.
     *
     * @param thread the thread currently executing {@link #execute}
     * @throws IllegalArgumentException if {@code thread} is null
     */
    protected void doInterrupt(Thread thread) {
        if (thread == null)
            throw new IllegalStateException("null thread");
        thread.interrupt();
    }
}
