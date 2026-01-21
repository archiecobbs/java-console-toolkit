
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.demo.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.simple.AbstractSimpleCommand;

/**
 * An "exec" command that forks off a {@link Process}.
 */
public class ExecCommand extends AbstractSimpleCommand {

    public static final int FAILED_TO_EXECUTE_ERROR_CODE = 126;     // bash "Cannot execute" error code

    public ExecCommand() {
        super("exec", "Execute an arbitrary system command.",
          "Executes a system command using a new java.lang.Process, inheriting the permissions of the Java process.");
    }

    @Override
    public int execute(ConsoleSession<?, ?> session, String name, List<String> args) throws InterruptedException {

        // Check usage
        if (args.isEmpty()) {
            this.printUsage(session, name);
            return 1;
        }

        // Fork it
        final Process process;
        try {
            process = new ProcessBuilder(args).start();
        } catch (IOException e) {
            e.printStackTrace(session.getErrorStream());
            return FAILED_TO_EXECUTE_ERROR_CODE;
        }

        // Do the I/O and wait for process to exit
        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            try {

                // Start the I/O threads
                executorService.submit(() -> this.transferFrom(session.getInputStream(), process::getOutputStream));
                executorService.submit(() -> this.transferTo(session.getOutputStream(), process::getInputStream));
                executorService.submit(() -> this.transferTo(session.getErrorStream(), process::getErrorStream));

                // Wait for process to exit
                return process.waitFor();
            } finally {
                process.destroyForcibly();
            }
        } finally {

            // Clean up executor service
            executorService.shutdownNow();
            executorService.awaitTermination(1000, TimeUnit.DAYS);
        }
    }

    // For stdin
    private void transferFrom(InputStream input, Supplier<OutputStream> supplier) {
        try (OutputStream output = supplier.get()) {
            input.transferTo(output);
        } catch (IOException e) {
            // ignore
        }
    }

    // For stdout and stderr
    private void transferTo(OutputStream output, Supplier<InputStream> supplier) {
        try (InputStream input = supplier.get()) {
            input.transferTo(output);
        } catch (IOException e) {
            // ignore
        }
    }
}
