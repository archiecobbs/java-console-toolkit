
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.List;

/**
 * Parses a command line string into a command name and parameters.
 */
public interface CommandLineParser {

    /**
     * Parse the given command line into a command name and parameters.
     *
     * <p>
     * If the line contains a syntax error, then a {@link SyntaxException} should be thrown.
     *
     * <p>
     * If the line does not contain even a command name, for example is pure whitespace,
     * then an empty list should be returned.
     *
     * <p>
     * If the line contains ends with an incomplete parse (e.g., with a backslash continuation character),
     * then null should be returned. In this case the caller should read another line of input, append it
     * to the original (after adding a line terminator), and then invoke this method again.
     *
     * @param line command line
     * @return parsed command line
     * @throws SyntaxException if {@code line} contains a syntax error
     * @throws IllegalArgumentException if {@code line} is null
     */
    List<String> parseCommandLine(String line) throws SyntaxException;

// SyntaxException

    /**
     * Exception thrown by a {@link CommandLineParser} when the line cannot be successfully parsed.
     */
    @SuppressWarnings("serial")
    class SyntaxException extends Exception {

        private final int offset;

        /**
         * Constructor.
         *
         * @param offset character offset of the error
         * @param message error description
         */
        public SyntaxException(int offset, String message) {
            super(message);
            this.offset = offset;
        }

        /**
         * Constructor.
         *
         * @param offset character offset of the error
         * @param message error description
         * @param cause chained exception
         */
        public SyntaxException(int offset, String message, Throwable cause) {
            super(message, cause);
            this.offset = offset;
        }

        /**
         * Get the character offset of the error.
         *
         * @return error offset
         */
        public int getOffset() {
            return this.offset;
        }
    }
}
