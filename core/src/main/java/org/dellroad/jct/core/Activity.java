
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.io.Closeable;
import java.util.concurrent.Future;

/**
 * An asynchronous activity with three distinct lifecycle phases.
 *
 * <p>
 * Instances transition through the following three states in this order:
 * <ul>
 *  <li>Active</li>
 *  <li>Inactive</li>
 *  <li>Closed</li>
 * </ul>
 *
 * <p>
 * Instances must always be {@link #close}'ed when no longer needed to release resources.
 *
 * <p>
 * For some instances, there may actually be no meaninful "Inactive" state: such instances
 * stay active until {@link #close} is invoked.
 *
 * <p>
 * Classes implementing this interface must be thread safe, at least with respect to the methods defined herein.
 */
public interface Activity extends Closeable {

    /**
     * Send an interrupt signal to this activity.
     *
     * <p>
     * This method no effect unless this instance is {@linkplain #isActive active}.
     *
     * <p>
     * How this method is handled is up to the activity: it may cause an active
     * activity to become inactive, or it may be ignored, or something in between.
     * In general, it should have roughly the same effect as one would expect when
     * pressing Control-C on a controlling terminal.
     *
     * @return true if activity was interrupted
     */
    boolean interrupt();

    /**
     * Determine whether this instance is still active.
     *
     * @return true if this instance is still active
     */
    boolean isActive();

    /**
     * Determine whether this instance has been closed.
     *
     * @return true if {@link #close} has been invoked
     */
    boolean isClosed();

    /**
     * Obtain a {@link Future} representing the completion of this activity.
     *
     * <p>
     * The future becomes complete when this activity is no longer {@linkplain #isActive active}.
     *
     * <p>
     * Invoking the future's {@link Future#cancel cancel()} method is equivalent to invoking
     * {@link #interrupt} (the {@code mayInterruptIfRunning} parameter is ignored).
     *
     * @return completion future
     */
    Future<?> getCompletionFuture();

// Closeable

    /**
     * Close this instance and release any associated resources.
     *
     * <p>
     * If this instance is still {@linkplain #isActive active}, it will be stopped.
     * After invoking this method, {@link #isActive} always returns false and
     * {@link #isClosed} always returns true.
     *
     * <p>
     * Does nothing if already closed.
     */
    @Override
    void close();
}
