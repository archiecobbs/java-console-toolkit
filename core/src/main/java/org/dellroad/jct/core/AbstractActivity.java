
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support superclass for {@link Activity} implementations.
 *
 * <p>
 * Subclasses implement {@link #performActivity} to execute the activity asynchronously. The asynchronous thread is
 * either provided by the {@link ExecutorService} passed to the constructor, or an automatically created one (via
 * {@link Executors#newSingleThreadExecutor}) if the default constructor is used. In the latter case, the automatically
 * created {@link ExecutorService} is shutdown when this activity is {@link #close}'d.
 *
 * <p>
 * The activity must be explicitly started via {@link #start}.
 */
public abstract class AbstractActivity implements Activity {

    public static final long DEFAULT_SHUTDOWN_WAIT_MILLIS = 1000;

    // Internal state
    enum State {
        INITIAL,
        STARTED,
        COMPLETED,
        CLOSED
    };

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor;
    private final boolean shutdownExecutor;

    private State state;
    private Future<?> future;
    private Thread activityThread;
    private long shutdownWait;

// Constructors

    /**
     * Default constructor.
     *
     * <p>
     * This instance will create its own {@link ExecutorService} using {@link Executors#newSingleThreadExecutor},
     * and that {@link ExecutorService} will be shutdown when this activity is {@link #close}'ed.
     */
    protected AbstractActivity() {
        this(Executors.newSingleThreadExecutor(), true);
    }

    /**
     * Constructor taking an external {@link ExecutorService}.
     *
     * <p>
     * The given {@link ExecutorService} will <i>not</i> be shutdown when this activity is {@link #close}'ed.
     *
     * @param executor the {@link ExecutorService} to use for performing this activity
     * @throws IllegalArgumentException if {@code executor} is null
     */
    protected AbstractActivity(ExecutorService executor) {
        this(executor, false);
        if (executor == null)
            throw new IllegalStateException("null executor");
    }

    private AbstractActivity(ExecutorService executor, boolean shutdownExecutor) {
        this.executor = executor;
        this.shutdownExecutor = shutdownExecutor;
        synchronized (this) {
            this.state = State.INITIAL;
            this.shutdownWait = DEFAULT_SHUTDOWN_WAIT_MILLIS;
        }
    }

    /**
     * Configure how long {@link #close} should wait for an automatically created {@link ExecutorService}
     * to shutdown before giving up.
     *
     * <p>
     * This setting has no effect unless the default constructor was used.
     *
     * <p>
     * Default is {@link #DEFAULT_SHUTDOWN_WAIT_MILLIS} milliseconds.
     *
     * @param millis how long to wait for executor shutdown in milliseconds
     * @throws IllegalArgumentException if {@code millis} is negative
     */
    public synchronized void setShutdownWait(long millis) {
        if (millis < 0)
            throw new IllegalArgumentException("millis < 0");
        this.shutdownWait = millis;
    }

    /**
     * Start this activity.
     *
     * <p>
     * This will result in {@link #performActivity} being invoked (asynchronously).
     *
     * @throws IllegalStateException if {@link #start} has already been invoked
     */
    public synchronized void start() {
        if (this.state != State.INITIAL)
            throw new IllegalStateException("already started");
        this.state = State.STARTED;
        this.future = this.executor.submit(this::performActivityWrapper);
        this.notifyAll();
    }

    private Void performActivityWrapper() throws Exception {
        synchronized (this) {
            if (this.state == State.CLOSED)
                return null;
            assert this.state == State.STARTED;
            if (Thread.interrupted()) {
                this.state = State.COMPLETED;
                return null;
            }
            this.activityThread = Thread.currentThread();
        }
        try {
            this.performActivity();
            return null;
        } catch (InterruptedException e) {
            return null;
        } catch (Throwable e) {
            try {
                this.handleException(e);
            } catch (Throwable t2) {
                // ignore
            }
            if (e instanceof Exception)
                throw (Exception)e;
            if (e instanceof Error)
                throw (Error)e;
            throw new RuntimeException(e);      // should never get here
        } finally {
            try {
                this.close();
            } finally {
                synchronized (this) {
                    this.activityThread = null;
                }
            }
        }
    }

// Subclass Hooks

    /**
     * Perform this activity.
     *
     * <p>
     * The current thread will be an {@link ExecutorService} thread.
     *
     * @throws InterruptedException if the current thread is interrupted
     * @throws Exception if some other error occurs
     */
    protected abstract void performActivity() throws Exception;

    /**
     * Handle an exception thrown by {@link performActivity}.
     *
     * <p>
     * The implementation in {@link AbstractActivity} does nothing. Subclasses may want to override to log an error, etc.
     *
     * <p>
     * The current thread will be an {@link ExecutorService} thread.
     *
     * @param t the exception thrown by {@link #performActivity}; never {@link InterruptedException}
     */
    protected void handleException(Throwable t) {
    }

    /**
     * Invoked by {@link #close} to allow the subclass to release resources.
     *
     * <p>
     * This method is guaranteed to only be invoked once per instance.
     * The implementation in {@link AbstractActivity} does nothing.
     */
    protected void internalClose() {
    }

// Activity

    @Override
    public synchronized boolean interrupt() {
        return this.state == State.STARTED && this.future.cancel(true);
    }

    @Override
    public synchronized boolean isActive() {
        return this.state.compareTo(State.COMPLETED) < 0;
    }

    @Override
    public synchronized boolean isClosed() {
        return this.state == State.CLOSED;
    }

    @Override
    public Future<?> getCompletionFuture() {
        return new CompletionFuture();
    }

    @Override
    public void close() {
        final boolean doShutdownExecutor;
        synchronized (this) {
            if (this.state == State.CLOSED)
                return;
            this.state = State.CLOSED;
            doShutdownExecutor = this.shutdownExecutor;
        }
        try {
            if (doShutdownExecutor) {
                this.executor.shutdownNow();
                if (Thread.currentThread() != this.activityThread) {
                    try {
                        this.executor.awaitTermination(this.shutdownWait, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } finally {
            this.internalClose();
        }
    }

// "Future forwarding" - required because this.future is null before start() is invoked

    private boolean futureCancel(boolean mayInterruptIfRunning) {
        return this.interrupt();
    }

    private Object futureGet() throws InterruptedException, ExecutionException {
        Future<?> f;
        synchronized (this) {
            while ((f = this.future) == null)
                this.wait();
        }
        return f.get();
    }

    private Object futureGet(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Future<?> f;
        long timeoutNanos = unit.toNanos(timeout);
        synchronized (this) {
            while ((f = this.future) == null) {
                if (timeoutNanos <= 0)
                    throw new TimeoutException();
                final long startNanos = System.nanoTime();
                TimeUnit.NANOSECONDS.timedWait(this, timeoutNanos);
                timeoutNanos -= System.nanoTime() - startNanos;
            }
        }
        return f.get(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    private boolean futureIsCancelled() {
        final Future<?> f;
        synchronized (this) {
            if ((f = this.future) == null)
                return false;
        }
        return f.isCancelled();
    }

    private boolean futureIsDone() {
        final Future<?> f;
        synchronized (this) {
            if ((f = this.future) == null)
                return false;
        }
        return f.isDone();
    }

// CompletionFuture

    private class CompletionFuture implements Future<Object> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return AbstractActivity.this.futureCancel(mayInterruptIfRunning);
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return AbstractActivity.this.futureGet();
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return AbstractActivity.this.futureGet(timeout, unit);
        }

        @Override
        public boolean isCancelled() {
            return AbstractActivity.this.futureIsCancelled();
        }

        @Override
        public boolean isDone() {
            return AbstractActivity.this.futureIsDone();
        }
    }
}
