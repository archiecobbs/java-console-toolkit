
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

/**
 * Support superclass for {@link JctSession} implementations.
 */
public abstract class AbstractJctSession implements JctSession {

    protected final JctConsole console;

    private Thread executionThread;
    private boolean executed;

    /**
     * Constructor.
     *
     * @param console associated console
     * @throws IllegalArgumentException if {@code console} is null
     */
    protected AbstractJctSession(JctConsole console) {
        if (console == null)
            throw new IllegalArgumentException("null console");
        this.console = console;
    }

// JctSession

    @Override
    public JctConsole getConsole() {
        return this.console;
    }

    @Override
    public boolean execute() throws InterruptedException {
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
        return this.executed && this.executionThread != null && this.doInterrupt(this.executionThread);
    }

// Subclass Methods

    /**
     * Execute this session.
     *
     * <p>
     * This method is invoked by {@link #execute} to do the actual work.
     *
     * @return true if successful, false if an error occurred
     * @throws InterruptedException if the current thread is interrupted
     */
    protected abstract boolean doExecute() throws InterruptedException;

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
