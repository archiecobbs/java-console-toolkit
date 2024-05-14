
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import jdk.jshell.execution.DirectExecutionControl;
import jdk.jshell.execution.LoaderDelegate;
import jdk.jshell.execution.LocalExecutionControl;

/**
 * A {@link LocalExecutionControl} that provides the ability to add thread-local context to the execution of
 * each JShell snippet and its subsequent result decoding into a {@link String}.
 *
 * <p>
 * It is sometimes useful to have the execution of JShell snippets occur within some thread-local context. For example,
 * a database application might want to open a transaction around each snippet execution. This is somewhat tricky because
 * JShell's {@link LocalExecutionControl} executes snippets in a different thread from the main command loop thread so,
 * for example, using {@link LocalExecutionControl#clientCodeEnter clientCodeEnter()} and
 * {@link LocalExecutionControl#clientCodeLeave clientCodeLeave()} won't work.
 *
 * <p>
 * In addition, context may also be necessary during the conversion of the snippet result into a {@link String} for display
 * in the JShell console. This conversion is performed by {@link DirectExecutionControl#valueString} and is normally done
 * later, in a separate thread. This class performs this conversion earlier, in the same thread that executes the snippet,
 * while the context is still available.
 *
 * <p>
 * The methods a subclass can use to bracket snippet execution and {@link String} result decoding are {@link #enterContext}
 * and {@code #leaveContext leaveContext()}.
 */
public class LocalContextExecutionControl extends LocalExecutionControl {

    private static final HashMap<ThreadGroup, InvokeInfo> SNIPPET_INVOKE_MAP = new HashMap<>();
    private static final Method INVOKE_WRAPPER_METHOD;
    static {
        try {
            INVOKE_WRAPPER_METHOD = LocalContextExecutionControl.class.getDeclaredMethod("invokeWrapper");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("internal error");
        }
    }

// Constructor

    /**
     * Constructor.
     *
     * @param delegate loader delegate
     */
    public LocalContextExecutionControl(LoaderDelegate delegate) {
        super(delegate);
    }

// LocalExecutionControl

    // This is a total hack. Our goal is to access this LocalContextExecutionControl from within the thread
    // that is actually invoking the target method (which is different from the current thread here). The problem
    // is the target method must be a static method, so we have to stash "this" somewhere. To accomplish that
    // we invoke invokeWrapper() instead of "method". Our invokeWrapper() will be invoked in some random
    // thread whose ThreadGroup's parent ThreadGroup is also this current thread's ThreadGroup. We use that
    // tenuous connection to retrieve the original method given here and also our current "this" instance.
    @Override
    protected String invoke(Method method) throws Exception {

        // Create InvokeInfo info and its lookup key
        final InvokeInfo invokeInfo = new InvokeInfo(this, method);
        final ThreadGroup lookupKey = Thread.currentThread().getThreadGroup();

        // Wait for any other snippet thread in my ThreadGroup to finish (can there be any?)
        synchronized (SNIPPET_INVOKE_MAP) {
            while (SNIPPET_INVOKE_MAP.containsKey(lookupKey))
                SNIPPET_INVOKE_MAP.wait();
            SNIPPET_INVOKE_MAP.put(lookupKey, invokeInfo);
        }

        // Proceed to invoke the snippet
        try {
            return super.invoke(INVOKE_WRAPPER_METHOD);
        } finally {

            // Clean up in case somehow invokeWrapper() was never actually invoked
            final InvokeInfo invokeInfo2;
            synchronized (SNIPPET_INVOKE_MAP) {
                invokeInfo2 = SNIPPET_INVOKE_MAP.get(lookupKey);
                if (invokeInfo2 == invokeInfo)                      // use object equality to ensure it's ours
                    SNIPPET_INVOKE_MAP.remove(lookupKey);
            }
        }
    }

// Subclass Hooks

    /**
     * Open a new thread-local context for snippet execution.
     *
     * <p>
     * The implementation in {@link LocalContextExecutionControl} does nothing.
     */
    protected void enterContext() {
    }

    /**
     * Close the thread-local context previously opened by {@link #enterContext} in the current thread.
     *
     * <p>
     * The implementation in {@link LocalContextExecutionControl} does nothing.
     *
     * @param success true if snippet execution was successful, false if an exception occurred
     */
    protected void leaveContext(boolean success) {
    }

// Internal Methods

    /**
     * Execute the given snippet in context.
     *
     * <p>
     * The implementation in {@link LocalContextExecutionControl} uses {@link #enterContext} and
     * {@link #leaveContext leaveContext()} to bracket the execution of the snippet and
     * the decoding of its result (via {@link #decodeWithContext decodeWithContext()}). The decoded
     * result is wrapped in a {@link StringWrapper} to prevent duplicate decoding by JShell.
     *
     * @param method static snippet method
     * @return result from snippet execution
     */
    protected Object invokeWithContext(Method method) throws Throwable {
        this.enterContext();
        boolean success = false;
        try {
            Object result = method.invoke(null);
            if (result != null)
                result = new StringWrapper(this.decodeWithContext(result));
            success = true;
            return result;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            this.leaveContext(success);
        }
    }

    /**
     * Decode the snippet result into a {@link String} for display in the JShell console.
     *
     * <p>
     * The implementation in {@link LocalContextExecutionControl} just invokes {@link DirectExecutionControl#valueString}.
     */
    protected String decodeWithContext(Object result) {
        return DirectExecutionControl.valueString(result);
    }

// Invoke Wrapper

    /**
     * Invocation wrapper method.
     *
     * <p>
     * This method recovers the current instance and the actual method to invoke from our internal secret mapping
     * and then delegates to {@link #invokeWithContext invokeWithContext()}.
     *
     * <p>
     * This method is only used internally but is required to be public due to Java access controls.
     *
     * @return result from snippet execution
     */
    public static Object invokeWrapper() throws Throwable {

        // Get the target method info stashed by invoke()
        final ThreadGroup lookupKey = Thread.currentThread().getThreadGroup().getParent();
        final InvokeInfo invokeInfo;
        synchronized (SNIPPET_INVOKE_MAP) {
            invokeInfo = SNIPPET_INVOKE_MAP.remove(lookupKey);
        }
        if (invokeInfo == null)
            throw new RuntimeException("internal error: snippet info not found");

        // Proceed
        return invokeInfo.getControl().invokeWithContext(invokeInfo.getMethod());
    }

// InvokeInfo

    private static final class InvokeInfo {

        private final LocalContextExecutionControl control;
        private final Method method;

        InvokeInfo(LocalContextExecutionControl control, Method method) {
            this.control = control;
            this.method = method;
        }

        public LocalContextExecutionControl getControl() {
            return this.control;
        }
        public Method getMethod() {
            return this.method;
        }
    }
}
