
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import com.google.common.base.Preconditions;

import jdk.jshell.execution.DirectExecutionControl;

/**
 * This is a simple wrapper around a {@link String} where {@link #toString} returns the wrapped {@link String}.
 *
 * <p>
 * The purpose of this class is simply to be unrecognized by JShell when {@link DirectExecutionControl#valueString}
 * displays the return value from some operation. Normally when that method sees a {@link String}, it displays it as
 * if it were a Java literal, adding double quotes and backslashes. If you instead wrap that {@link String} in this class,
 * then JShell will display it as-is.
 */
public final class StringWrapper {

    private final String string;

    /**
     * Constructor.
     *
     * @param string the actual string
     * @throws IllegalArgumentException if {@code string} is null
     */
    public StringWrapper(String string) {
        Preconditions.checkState(string != null, "null string");
        this.string = string;
    }

    /**
     * Returns the wrapped string.
     *
     * @return wrapped string
     */
    @Override
    public String toString() {
        return this.string;
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        final StringWrapper that = (StringWrapper)obj;
        return this.string.equals(that.string);
    }
}
