
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A simple command line parser:
 * <ul>
 *  <li>Parameters are separated by whitespace
 *  <li>Java-style doubly quoted string literals are supported
 * </ul>
 */
public class SimpleCommandLineParser implements Function<String, List<String>> {

    @Override
    public List<String> apply(String line) {
        if (line == null)
            throw new IllegalArgumentException("null line");
        final ArrayList<String> argList = new ArrayList<>();
        final int length = line.length();
        boolean inquote = false;
        int posn = 0;
        StringBuilder nextArg = null;
        while (posn < length) {
            final char ch = line.charAt(posn++);

            // Handle unquoted state
            if (!inquote) {
                if (Character.isWhitespace(ch)) {
                    if (nextArg != null) {              // end of word
                        argList.add(nextArg.toString());
                        nextArg = null;
                    }
                    continue;
                }
                if (nextArg == null)                    // start of word
                    nextArg = new StringBuilder();
                if (ch == '"') {
                    inquote = true;
                    continue;
                }
                nextArg.append(ch);
                continue;
            }

            // Handle quoted state
            if (ch == '"') {
                inquote = false;
                continue;
            }
            posn = this.scanQuotedChar(line, ch, posn, nextArg);
        }
        if (inquote)
            throw this.makeUnclosedQuoteError();
        if (nextArg != null)
            argList.add(nextArg.toString());
        return argList;
    }

    private int scanQuotedChar(String line, char ch, int posn, StringBuilder arg) {
        final int length = line.length();
        switch (ch) {
        case '\r':
            throw this.makeError("illegal carriage return within string literal");
        case '\n':
            throw this.makeError("illegal newline within string literal");
        case '\\':
            if (posn >= length)
                throw this.makeUnclosedQuoteError();
            boolean simpleEscape = true;
            switch ((ch = line.charAt(posn++))) {
            case 'b':
                ch = '\b';
                break;
            case 't':
                ch = '\t';
                break;
            case 'n':
                ch = '\n';
                break;
            case 'f':
                ch = '\f';
                break;
            case 'r':
                ch = '\r';
                break;
            case '\'':
            case '\"':
            case '\\':
                break;
            default:
                simpleEscape = false;
                break;
            }
            if (simpleEscape) {
                arg.append(ch);
                return posn;
            }

            // Decode octal escape
            int charValue = Character.digit(ch, 8);
            if (charValue != -1) {
                for (int i = 0; i < 2 && (charValue & ~0x1f) == 0; i++) {
                    if (posn >= length)
                        throw this.makeError("truncated octal escape");
                    ch = line.charAt(posn++);
                    final int oct = Character.digit(ch, 8);
                    if (oct == -1)
                        break;
                    charValue = (charValue << 3) | oct;
                }
                arg.append((char)charValue);
                return posn;
            }

            // Decode Unicode escape
            if (ch == 'u') {
                charValue = 0;
                for (int i = 0; i < 4; i++) {
                    if (posn >= length)
                        throw this.makeError("truncated Unicode escape");
                    ch = line.charAt(posn++);
                    if (i == 0 && ch == 'u') {      // allow arbitrarily many 'u' characters per JLS
                        i--;
                        continue;
                    }
                    final int hex = Character.digit(ch, 16);
                    if (hex == -1)
                        throw this.makeError("invalid Unicode escape");
                    charValue = (charValue << 4) | hex;
                }
                arg.append((char)charValue);
                return posn;
            }

            // Bogus
            throw this.makeError("invalid backslash escape");

        default:
            arg.append(ch);
            return posn;
        }
    }

    private IllegalArgumentException makeUnclosedQuoteError() {
        return this.makeError("unclosed string literal");
    }

    private IllegalArgumentException makeError(String message) {
        return new IllegalArgumentException(message);
    }
}
